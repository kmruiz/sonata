#pragma once

#include <memory>
#include <variant>

#include "../parser.h"
#include "lexer.h"
#include "ast.h"

namespace scc::parser::test {
    using std::shared_ptr;

    template <int ChildIndex, class ChildType>
    inline shared_ptr<ChildType> child_nth(const ast::ast_root &root) {
        int idx = 0;
        for (auto &x : root->children) {
            if (idx == ChildIndex) {
                return std::dynamic_pointer_cast<ChildType>(x);
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex>
    inline shared_ptr<nclass_primary_field> field_nth(const shared_ptr<ast::nclass> &root) {
        int idx = 0;
        for (auto &x : root->fields) {
            if (idx == ChildIndex) {
                return x;
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex, class ChildType>
    inline shared_ptr<ChildType> child_nth(const shared_ptr<ast::nclass> &root) {
        int idx = 0;
        if (!root->body.has_value()) {
            return nullptr;
        }

        for (auto &x : root->body.value()->children) {
            if (idx == ChildIndex) {
                return std::dynamic_pointer_cast<ChildType>(x);
            }

            idx++;
        }

        return nullptr;
    }

    template <class ChildType>
    inline shared_ptr<ChildType> body_of(const shared_ptr<ast::nlet> &root) {
        return std::dynamic_pointer_cast<ChildType>(root->expression.value());
    }

    template <class LeftType>
    inline shared_ptr<LeftType> left_op(const shared_ptr<ast::nfunction_call> &root) {
        return std::dynamic_pointer_cast<LeftType>(root->left);
    }

    template <class LeftType>
    inline shared_ptr<LeftType> left_op(const shared_ptr<ast::nmethod_call> &root) {
        return std::dynamic_pointer_cast<LeftType>(root->left);
    }

    template <int ChildIndex, class ArgType>
    inline std::shared_ptr<typename std::enable_if<not std::is_same<ArgType, nfunction_call_named_argument>::value, ArgType>::type>
    argument_nth(const shared_ptr<ast::nfunction_call> &root) {
        int idx = 0;
        for (auto &x : root->arguments) {
            if (idx == ChildIndex) {
                if (std::holds_alternative<expression_ref>(x)) {
                    return std::dynamic_pointer_cast<ArgType>(std::get<expression_ref>(x));
                }
                break;
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex, class ArgType>
    inline std::shared_ptr<typename std::enable_if<not std::is_same<ArgType, nfunction_call_named_argument>::value, ArgType>::type>
    argument_nth(const shared_ptr<ast::nmethod_call> &root) {
        int idx = 0;
        for (auto &x : root->arguments) {
            if (idx == ChildIndex) {
                if (std::holds_alternative<expression_ref>(x)) {
                    return std::dynamic_pointer_cast<ArgType>(std::get<expression_ref>(x));
                }
                break;
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex, class ArgType>
    shared_ptr<typename std::enable_if<std::is_same<ArgType, nfunction_call_named_argument>::value, ArgType>::type>
    inline argument_nth(const shared_ptr<ast::nfunction_call> &root) {
        int idx = 0;
        for (auto &x : root->arguments) {
            if (idx == ChildIndex) {
                if (std::holds_alternative<nfunction_call_named_argument_ref>(x)) {
                    return std::get<nfunction_call_named_argument_ref>(x);
                }

                break;
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex, class ArgType>
    shared_ptr<typename std::enable_if<std::is_same<ArgType, nfunction_call_named_argument>::value, ArgType>::type>
    inline argument_nth(const shared_ptr<ast::nmethod_call> &root) {
        int idx = 0;
        for (auto &x : root->arguments) {
            if (idx == ChildIndex) {
                if (std::holds_alternative<nfunction_call_named_argument_ref>(x)) {
                    return std::get<nfunction_call_named_argument_ref>(x);
                }

                break;
            }

            idx++;
        }

        return nullptr;
    }

    template <typename ArgType>
    inline std::shared_ptr<ArgType> argument_value(const nfunction_call_named_argument_ref &root) {
        return std::dynamic_pointer_cast<ArgType>(root->expression);
    }

    inline bool has_body(const shared_ptr<ast::nlet> &root) {
        return root->expression.has_value();
    }

    inline bool has_body(const shared_ptr<ast::nlet_function> &root) {
        return root->body.has_value();
    }

    inline bool has_parameters(const shared_ptr<ast::nlet_function> &root) {
        return !root->parameters.empty();
    }

    template <int ChildIndex, class ArgType>
    inline std::shared_ptr<typename std::enable_if<not std::is_same<ArgType, nlet_function_named_parameter>::value, ArgType>::type>
    parameter_nth(const shared_ptr<ast::nlet_function> &root) {
        int idx = 0;
        for (auto &x : root->parameters) {
            if (idx == ChildIndex) {
                if (std::holds_alternative<expression_ref>(x)) {
                    return std::dynamic_pointer_cast<ArgType>(std::get<expression_ref>(x));
                }
                break;
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex, class ArgType>
    shared_ptr<typename std::enable_if<std::is_same<ArgType, nlet_function_named_parameter>::value, ArgType>::type>
    inline parameter_nth(const shared_ptr<ast::nlet_function> &root) {
        int idx = 0;
        for (auto &x : root->parameters) {
            if (idx == ChildIndex) {
                if (std::holds_alternative<nlet_function_named_parameter_ref>(x)) {
                    return std::get<nlet_function_named_parameter_ref>(x);
                }

                break;
            }

            idx++;
        }

        return nullptr;
    }

    template <int ChildIndex, class ChildType>
    inline ChildType generic_parameter_nth(const type_constraints &root) {
        int idx = 0;

        for (auto &x : get<type_constraint_generic>(root).parameters) {
            if (idx == ChildIndex) {
                return get<ChildType>(x);
            }

            idx++;
        }

        throw std::runtime_error(std::string("Could not find a generic parameter with type ") + typeid(ChildType).name());
    }

    inline ast::ast_root parse(const string &code) {
        scc::lexer::lexer _lexer;
        scc::parser::parser _parser;
        std::stringstream ss(code);

        return _parser.parse(_lexer.process(ss, code));
    }
}