#include <tuple>

#include "parser.h"

#include "token.h"
#include "diagnostic.h"
#include "ast.h"

namespace scc::parser {
    using namespace ast;
    using namespace lexer;
    using std::tuple;
    using std::optional;
    using std::make_optional;
    using std::tie;
    using std::make_tuple;

    static const token_stream skip_whitespace(const token_stream &tokens);
    static const token_stream skip_whitespace_no_newline(const token_stream &tokens);
    static const token_stream next_token_stream(const token_stream &tokens);
    static const token_stream panic(const token_stream &tokens);
    static const token_stream assert_token_keyword(const string &kw, const token_stream &tokens);
    static const token_stream assert_token_type(const token_type &type, const token_stream &tokens);
    static tuple<node_ref, token_stream> parse_node(const token_stream &tokens);
    static tuple<expression_ref, token_stream> parse_expression(const token_stream &tokens);
    static tuple<node_ref, token_stream> parse_let_expression(const token_stream &tokens);
    static tuple<node_ref, token_stream> parse_let_function_definition(const string &name, const token_stream &tokens);
    static tuple<type_constraints, token_stream> parse_type_constraints(const token_stream &tokens);
    static tuple<expression_ref, token_stream> parse_function_call(const expression_ref &left, const token_stream &tokens);
    static tuple<node_ref, token_stream> parse_class(const token_stream &tokens);

    parser::parser() {

    }

    parser::~parser() {

    }

    ast_root parser::parse(const token_stream &tokens) {
        D_START_PHASE(diagnostic::diagnostic_phase_id::PARSER);
        auto _root = std::make_shared<root>();
        auto next_tokens = tokens;
        while (!next_tokens.empty()) {
            node_ref ref;
            tie(ref, next_tokens) = parse_node(next_tokens);
            next_tokens = skip_whitespace(next_tokens);

            if (!next_tokens.empty() && next_tokens.front()->type == token_type::END_OF_FILE) {
                next_tokens = next_token_stream(next_tokens);
            }

            _root->children.emplace_back(ref);
        }

        D_END_PHASE();
        return _root;
    }

    static const token_stream skip_whitespace(const token_stream &tokens) {
        for (auto c = tokens.begin(); c != tokens.end(); c++) {
            if ((*c)->type == token_type::WHITESPACE || (*c)->type == token_type::NEW_LINE) {
                continue;
            }

            return token_stream(c, tokens.end());
        }

        return {};
    }

    static const token_stream skip_whitespace_no_newline(const token_stream &tokens) {
        for (auto c = tokens.begin(); c != tokens.end(); c++) {
            if ((*c)->type == token_type::WHITESPACE) {
                continue;
            }

            return token_stream(c, tokens.end());
        }

        return {};
    }

    static const token_stream next_token_stream(const token_stream &tokens) {
        return token_stream(++tokens.begin(), tokens.end());
    }

    static const token_stream panic(const token_stream &tokens) {
        for (auto c = tokens.begin(); c != tokens.end(); c++) {
            if ((*c)->type == token_type::NEW_LINE) {
                return token_stream(c, tokens.end());
            }
        }

        return {};
    }

    static const token_stream assert_token_keyword(const string &kw, const token_stream &tokens) {
        const auto &current = tokens.front();
        if (current->type == token_type::IDENTIFIER) {
            const auto &token_data = std::get<info_identifier>(current->metadata).content;
            if (token_data == kw) {
                return next_token_stream(tokens);
            }

            D_ERROR("Expected identifier " + kw + " but " + token_data + " found.", {
                    diagnostic::diagnostic_log_marker{.key = "expected", .value = kw},
                    diagnostic::diagnostic_log_marker{.key = "found", .value = token_data},
            });

            return panic(tokens);
        }

        D_ERROR("Expected identifier " + kw + " but token type " + to_string(current->type) + " found.", {
                diagnostic::diagnostic_log_marker{.key = "expected", .value = kw},
                diagnostic::diagnostic_log_marker{.key = "found type", .value = to_string(current->type)},
        });

        return panic(tokens);
    }

    static const token_stream assert_token_type(const token_type &type, const token_stream &tokens) {
        const auto &current = tokens.front();
        if (current->type == type) {
            return next_token_stream(tokens);
        }

        D_ERROR("Expected token type " + to_string(type) + " but found token type " + to_string(current->type) +
                " found.", {
                        diagnostic::diagnostic_log_marker{.key = "expected type", .value = to_string(type)},
                        diagnostic::diagnostic_log_marker{.key = "found type", .value = to_string(current->type)},
                });

        return panic(tokens);
    }

    static tuple<node_ref, token_stream> parse_node(const token_stream &tokens) {
        if (tokens.empty()) {
            return make_tuple(nullptr, tokens);
        }

        const auto &current = tokens.front();
        if (current->type == token_type::IDENTIFIER) {
            const auto &token_data = std::get<info_identifier>(current->metadata).content;
            if (token_data == "let") {
                return parse_let_expression(next_token_stream(tokens));
            } else if (token_data == "capability" || token_data == "value" || token_data == "entity") {
                return parse_class(tokens);
            } else {
                return parse_expression(tokens);
            }
        }

        return make_tuple(nullptr, tokens);
    }

    static tuple<expression_ref, token_stream> parse_expression(const token_stream &tokens) {
        if (tokens.empty()) {
            return make_tuple(nullptr, tokens);
        }

        expression_ref result;
        auto next_tokens = skip_whitespace_no_newline(tokens);
        auto value = next_tokens.front();
        next_tokens = next_token_stream(next_tokens);

        if (value->type == token_type::INTEGER) {
            auto nconst = std::make_shared<nconstant>();
            nconst->type = ast::nconstant_type::INTEGER;
            nconst->content = get<info_integer>(value->metadata);
            result = nconst;
        } else if (value->type == token_type::FLOATING) {
            auto nconst = std::make_shared<nconstant>();
            nconst->type = ast::nconstant_type::FLOATING;
            nconst->content = get<info_floating>(value->metadata);
            result = nconst;
        } else if (value->type == token_type::IDENTIFIER) {
            auto ncontent = get<info_identifier>(value->metadata);
            if (ncontent.content == "true" || ncontent.content == "false") {
                auto nconst = std::make_shared<nconstant>();
                nconst->type = ast::nconstant_type::BOOLEAN;
                nconst->content = info_boolean{.value = get<info_identifier>(value->metadata).content == "true"};
                result = nconst;
            } else {
                auto nident = std::make_shared<nidentifier>();
                nident->name = ncontent.content;
                result = nident;
            }
        } else if (value->type == token_type::STRING) {
            auto nconst = std::make_shared<nconstant>();
            nconst->type = ast::nconstant_type::STRING;
            nconst->content = get<info_string>(value->metadata);
            result = nconst;
        }

        next_tokens = skip_whitespace_no_newline(next_tokens);
        auto paren_or_else = next_tokens.front();
        if (paren_or_else == nullptr) {
            return make_tuple(result, next_tokens);
        }

        if (paren_or_else->type == token_type::OPEN_PAREN) {
            tie(result, next_tokens) = parse_function_call(result, next_tokens);
        }

        return make_tuple(result, next_tokens);
    }

    // let [mutable] identifier[(...params)][: type definition] [= initial value]
    static tuple<node_ref, token_stream> parse_let_expression(const token_stream &tokens) {
        auto node = std::make_shared<ast::nlet>();
        node->constraints = type_constraint_none();

        auto next_tokens = skip_whitespace(tokens);
        auto name_or_mutable_token = next_tokens.front();

        if (name_or_mutable_token->type != token_type::IDENTIFIER) {
            D_ERROR("Unexpected token, expected identifier or 'mutable' keyword.", {
                    diagnostic::diagnostic_log_marker{
                            .key = "found token type", .value = to_string(name_or_mutable_token->type)
                    }, diagnostic::diagnostic_log_marker{
                            .key = "origin", .value = "parse_let_expression"
                    }, diagnostic::diagnostic_log_marker{
                            .key = "for", .value = "let <identifier>"
                    },
            });

            return make_tuple(nullptr, panic(next_tokens));
        }

        auto name_or_mutable_content = std::get<info_identifier>(name_or_mutable_token->metadata).content;
        if (name_or_mutable_content == "mutable") {
            node->mutable_p = true;
            next_tokens = skip_whitespace(next_token_stream(next_tokens));
            name_or_mutable_token = next_tokens.front();
        }

        if (name_or_mutable_token->type != token_type::IDENTIFIER) {
            D_ERROR("Unexpected token, expected identifier.", {
                    diagnostic::diagnostic_log_marker{
                            .key = "found token type", .value = to_string(name_or_mutable_token->type)
                    }, diagnostic::diagnostic_log_marker{
                            .key = "origin", .value = "parse_let_expression"
                    }, diagnostic::diagnostic_log_marker{
                            .key = "for", .value = "let <identifier>"
                    },
            });

            return make_tuple(nullptr, panic(next_tokens));
        }

        node->name = std::get<info_identifier>(name_or_mutable_token->metadata).content;
        next_tokens = skip_whitespace_no_newline(next_token_stream(next_tokens));

        auto colon_param_equals_or_else = next_tokens.front();
        if (colon_param_equals_or_else->type == token_type::COLON) {
            tie(node->constraints, next_tokens) = parse_type_constraints(
                    skip_whitespace_no_newline(next_token_stream(next_tokens)));
            next_tokens = skip_whitespace_no_newline(next_tokens);
            auto equal_or_else = next_tokens.front();
            if (equal_or_else->type == token_type::EQUALS) {
                tie(node->expression, next_tokens) = parse_expression(
                        skip_whitespace_no_newline(next_token_stream(next_tokens)));
                return make_tuple(node, next_tokens);
            } else if (equal_or_else->type == token_type::NEW_LINE || equal_or_else->type == token_type::END_OF_FILE) {
                return make_tuple(node, skip_whitespace(next_tokens));
            } else {
                D_ERROR("Unexpected token. Expected colon, equals or a new line.", {
                        diagnostic::diagnostic_log_marker{
                                .key = "found token type", .value = to_string(equal_or_else->type)
                        }, diagnostic::diagnostic_log_marker{
                                .key = "origin", .value = "parse_let_expression"
                        }, diagnostic::diagnostic_log_marker{
                                .key = "for", .value = "let " + std::string(node->mutable_p ? "mutable " : "") +
                                                       node->name +
                                                       " : TYPE <equals/new line>"
                        },
                });

                return make_tuple(nullptr, panic(next_tokens));
            }

        } else if (colon_param_equals_or_else->type == token_type::EQUALS) {
            tie(node->expression, next_tokens) = parse_expression(
                    skip_whitespace_no_newline(next_token_stream(next_tokens)));
            return make_tuple(node, next_tokens);
        } else if (colon_param_equals_or_else->type == token_type::NEW_LINE) {
            return make_tuple(node, skip_whitespace(next_tokens));
        } else if (colon_param_equals_or_else->type == token_type::OPEN_PAREN) {
            return parse_let_function_definition(node->name, next_token_stream(next_tokens));
        }

        D_ERROR("Unexpected token. Expected colon, equals or a new line.", {
                diagnostic::diagnostic_log_marker{
                        .key = "found token type", .value = to_string(colon_param_equals_or_else->type)
                }, diagnostic::diagnostic_log_marker{
                        .key = "origin", .value = "parse_let_expression"
                }, diagnostic::diagnostic_log_marker{
                        .key = "for", .value = "let " + std::string(node->mutable_p ? "mutable " : "") + node->name +
                                               " <colon/equals/new line>"
                },
        });

        return make_tuple(nullptr, panic(next_tokens));
    }

    static tuple<node_ref, token_stream> parse_let_function_definition(const string &name, const token_stream &tokens) {
        auto node = std::make_shared<ast::nlet_function>();
        node->name = name;
        node->return_type = type_constraint_none();
        auto next_tokens = skip_whitespace(tokens);

        if (next_tokens.front()->type == token_type::CLOSE_PAREN) {
            next_tokens = skip_whitespace(next_token_stream(next_tokens));
        } else {
            while (true) {
                auto param = std::make_shared<nlet_function_named_parameter>();
                next_tokens = skip_whitespace(next_tokens);
                auto field_name = next_tokens.front();
                if (field_name->type != token_type::IDENTIFIER) {
                    return make_tuple(nullptr, panic(next_tokens));
                }

                next_tokens = skip_whitespace(next_token_stream(next_tokens));
                next_tokens = assert_token_type(token_type::COLON, skip_whitespace(next_tokens));
                tie(param->type, next_tokens) = parse_type_constraints(skip_whitespace(next_tokens));
                next_tokens = skip_whitespace(next_tokens);

                param->name = get<info_identifier>(field_name->metadata).content;
                node->parameters.emplace_back(param);
                if (next_tokens.front()->type == token_type::CLOSE_PAREN) {
                    next_tokens = skip_whitespace(next_token_stream(next_tokens));
                    break;
                }

                next_tokens = assert_token_type(token_type::COMMA, next_tokens);
            }
        }

        auto colon_or_eq = next_tokens.front();
        if (colon_or_eq->type == token_type::COLON) {
            tie(node->return_type, next_tokens) = parse_type_constraints(
                    skip_whitespace_no_newline(next_token_stream(next_tokens)));
            next_tokens = skip_whitespace(next_tokens);
            colon_or_eq = next_tokens.front();
        }

        if (colon_or_eq->type == token_type::EQUALS) {
            tie(node->body, next_tokens) = parse_expression(skip_whitespace(next_token_stream(next_tokens)));
        }

        return make_tuple(node, skip_whitespace(next_tokens));
    }

    // <empty> : none
    // T       : equality
    // +T      : covariant (only in generic)
    // -T      : contravariant (only in generic)
    // A[B,C]  : generic
    // A | B   : sum
    // T       : constant (if T != identifier)
    static tuple<type_constraints, token_stream> parse_type_constraints(const token_stream &tokens) {
        if (tokens.front()->type == token_type::NEW_LINE) {
            return make_tuple(type_constraint_none(), tokens);
        }

        auto next_tokens = skip_whitespace(tokens);
        auto base = next_tokens.front();
        if (base->type != token_type::IDENTIFIER) {
            D_ERROR("Expected type identifier.", {
                    diagnostic::diagnostic_log_marker{
                            .key = "found token type", .value = to_string(base->type)
                    }, diagnostic::diagnostic_log_marker{
                            .key = "origin", .value = "parse_type_constraints"
                    }
            });

            return make_tuple(type_constraint_none(), panic(next_tokens));
        }

        string base_type_name = get<info_identifier>(base->metadata).content;
        return make_tuple(type_constraint_equality{.type = base_type_name}, next_token_stream(next_tokens));
    }

    static tuple<expression_ref, token_stream>
    parse_function_call(const expression_ref &left, const token_stream &tokens) {
        auto result = std::make_shared<nfunction_call>();
        result->left = left;

        auto next_tokens = next_token_stream(tokens);
        while (next_tokens.front()->type != token_type::CLOSE_PAREN) {
            // check if it's a named argument
            auto maybe_named_tokens = skip_whitespace(next_tokens);
            const auto name_p = maybe_named_tokens.front();
            maybe_named_tokens = next_token_stream(skip_whitespace(maybe_named_tokens));
            const auto equal_p = maybe_named_tokens.front();

            if (name_p->type == token_type::IDENTIFIER && equal_p->type == token_type::EQUALS) {
                auto named_arg = std::make_shared<nfunction_call_named_argument>();
                named_arg->name = get<info_identifier>(name_p->metadata).content;
                tie(named_arg->expression, next_tokens) = parse_expression(next_token_stream(maybe_named_tokens));
                result->arguments.emplace_back(named_arg);
            } else {
                expression_ref arg;
                tie(arg, next_tokens) = parse_expression(next_tokens);
                result->arguments.emplace_back(arg);
            }

            if (next_tokens.front()->type == token_type::COMMA) {
                next_tokens = next_token_stream(next_tokens);
            }
        }

        return make_tuple(result, next_token_stream(next_tokens));
    }

    static tuple<node_ref, token_stream> parse_class(const token_stream &tokens) {
        std::shared_ptr<nclass> result = std::make_shared<nclass>();

        auto next_tokens = tokens;
        auto class_type = get<info_identifier>(next_tokens.front()->metadata).content;
        if (class_type == "capability") {
            result->type = nclass_type::CAPABILITY;
        } else if (class_type == "value") {
            result->type = nclass_type::VALUE;
        } else if (class_type == "entity") {
            result->type = nclass_type::ENTITY;
        }

        next_tokens = skip_whitespace(next_token_stream(next_tokens));
        auto class_or_interface_kw = next_tokens.front();
        if (class_or_interface_kw->type != token_type::IDENTIFIER) {
            return make_tuple(nullptr, panic(next_tokens));
        }

        if (get<info_identifier>(class_or_interface_kw->metadata).content == "interface") {
            if (result->type == nclass_type::ENTITY) {
                result->type = nclass_type::ENTITY_INTERFACE;
            } else {
                return make_tuple(nullptr, panic(next_tokens));
            }
        } else if (get<info_identifier>(class_or_interface_kw->metadata).content != "class") {
            return make_tuple(nullptr, panic(next_tokens));
        }

        next_tokens = skip_whitespace(next_token_stream(next_tokens));
        result->name = std::get<info_identifier>(next_tokens.front()->metadata).content;
        next_tokens = skip_whitespace(next_token_stream(next_tokens));

        if (result->type != nclass_type::ENTITY_INTERFACE) {
            next_tokens = assert_token_type(token_type::OPEN_PAREN, next_tokens);
            if (next_tokens.front()->type == token_type::CLOSE_PAREN) {
                return make_tuple(result, next_token_stream(next_tokens));
            }

            while (true) {
                auto field = std::make_shared<nclass_primary_field>();
                next_tokens = skip_whitespace(next_tokens);
                auto field_name = next_tokens.front();
                if (field_name->type != token_type::IDENTIFIER) {
                    return make_tuple(nullptr, panic(next_tokens));
                }

                next_tokens = skip_whitespace(next_token_stream(next_tokens));
                next_tokens = assert_token_type(token_type::COLON, skip_whitespace(next_tokens));
                tie(field->type, next_tokens) = parse_type_constraints(skip_whitespace(next_tokens));
                next_tokens = skip_whitespace(next_tokens);

                field->name = get<info_identifier>(field_name->metadata).content;
                result->fields.emplace_back(field);
                if (next_tokens.front()->type == token_type::CLOSE_PAREN) {
                    break;
                }

                next_tokens = assert_token_type(token_type::COMMA, next_tokens);
            }
            next_tokens = skip_whitespace(assert_token_type(token_type::CLOSE_PAREN, next_tokens));
        }

        if (next_tokens.front()->type == token_type::OPEN_BRACE) {
            result->body = std::make_shared<block>();
            next_tokens = skip_whitespace(next_token_stream(next_tokens));

            while (true) {
                node_ref child;
                tie(child, next_tokens) = parse_node(next_tokens);
                result->body.value()->children.emplace_back(child);
                next_tokens = skip_whitespace(next_tokens);

                if (next_tokens.front()->type == token_type::CLOSE_BRACE) {
                    next_tokens = skip_whitespace(next_token_stream(next_tokens));
                    break;
                }
            }
        }

        return make_tuple(result, next_tokens);
    }
}