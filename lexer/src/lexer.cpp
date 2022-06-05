#include "lexer.h"

#include <sstream>

#include "diagnostic.h"

static inline std::shared_ptr<scc::lexer::token> simple_token(scc::lexer::token_type type) {
    auto token = std::make_shared<scc::lexer::token>();
    token->type = type;

    return token;
}

namespace scc::lexer {
    lexer::lexer() {

    }

    lexer::~lexer() {

    }

    token_stream lexer::process(istream &istream, const string &name) {
        D_START_PHASE(diagnostic::diagnostic_phase_id::LEXER);
        D_DEBUG("Lexer started processing source.", { diagnostic::diagnostic_log_marker { .key = "source", .value = name }});

        auto result = token_stream();
        std::stringstream accumulator;

        auto rchar = (unsigned char) istream.get();
        do {
            switch (rchar) {
                case ';': { // accumulate until new line
                    accumulator << rchar;
                    do {
                        rchar = istream.get();
                        if (rchar == '\n' || !istream.good()) {
                            auto comment = std::make_shared<token>();
                            comment->type = token_type::COMMENT;
                            comment->metadata = info_comment{.content = accumulator.str()};

                            result.push_back(comment);
                            result.push_back(simple_token(token_type::NEW_LINE));
                            break;
                        }

                        accumulator << rchar;
                    } while (istream.good());

                    accumulator.str("");
                }
                    break;
                case ' ':
                case '\t':
                    result.push_back(simple_token(token_type::WHITESPACE));
                    break;
                case '\n':
                    result.push_back(simple_token(token_type::NEW_LINE));
                    break;
                case '(':
                    result.push_back(simple_token(token_type::OPEN_PAREN));
                    break;
                case ')':
                    result.push_back(simple_token(token_type::CLOSE_PAREN));
                    break;
                case '[':
                    result.push_back(simple_token(token_type::OPEN_BRACKET));
                    break;
                case ']':
                    result.push_back(simple_token(token_type::CLOSE_BRACKET));
                    break;
                case '{':
                    result.push_back(simple_token(token_type::OPEN_BRACE));
                    break;
                case '}':
                    result.push_back(simple_token(token_type::CLOSE_BRACE));
                    break;
                case ',':
                    result.push_back(simple_token(token_type::COMMA));
                    break;
                case ':':
                    result.push_back(simple_token(token_type::COLON));
                    break;
                case '.':
                    result.push_back(simple_token(token_type::DOT));
                    break;
                case '@':
                    result.push_back(simple_token(token_type::AT));
                    break;
                case '#':
                    result.push_back(simple_token(token_type::HASH));
                    break;
                case '~':
                    result.push_back(simple_token(token_type::TILDE));
                    break;
                case '?':
                    result.push_back(simple_token(token_type::QUESTION_MARK));
                    break;
                case '!':
                    result.push_back(simple_token(token_type::EXCLAMATION_MARK));
                    break;
                case '=':
                    result.push_back(simple_token(token_type::EQUALS));
                    break;
                case '<':
                    result.push_back(simple_token(token_type::LESS_THAN));
                    break;
                case '>':
                    result.push_back(simple_token(token_type::GREATER_THAN));
                    break;
                case '+':
                    result.push_back(simple_token(token_type::PLUS));
                    break;
                case '-':
                    result.push_back(simple_token(token_type::MINUS));
                    break;
                case '*':
                    result.push_back(simple_token(token_type::MULTIPLY));
                    break;
                case '/':
                    result.push_back(simple_token(token_type::DIVIDE));
                    break;
                case '^':
                    result.push_back(simple_token(token_type::CARET));
                    break;
                case '%':
                    result.push_back(simple_token(token_type::PERCENT));
                    break;
                case '&':
                    result.push_back(simple_token(token_type::AMPERSAND));
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    accumulator << rchar;

                    numeric_base base = numeric_base::DECIMAL;
                    auto floating = false;

                    auto basechar = (unsigned char) istream.get();
                    switch (basechar) {
                        case 'x':
                        case 'X':
                            base = numeric_base::HEXADECIMAL;
                            accumulator << basechar;
                            break;
                        case 'o':
                        case 'O':
                            base = numeric_base::OCTAL;
                            accumulator << basechar;
                            break;
                        case 'b':
                        case 'B':
                            base = numeric_base::BINARY;
                            accumulator << basechar;
                            break;
                        default:
                            if (!(basechar >= '0' && basechar <= '9') && basechar != '.') {
                                auto id = std::make_shared<token>();
                                id->type = token_type::INTEGER;
                                id->metadata = info_integer{.representation = accumulator.str(), .base = numeric_base::DECIMAL};
                                result.push_back(id);

                                accumulator.str("");
                                rchar = basechar;
                                goto skip_reading;
                            }

                            if (basechar == '.') {
                                floating = true;
                            }

                            accumulator << basechar;
                    }

                    do {
                        rchar = istream.get();

                        if (!(
                                (rchar >= '0' && rchar <= '9')
                        )) {
                            if (rchar == '.' && base == numeric_base::DECIMAL && !floating) {
                                floating = true;
                            } else {
                                auto number = std::make_shared<token>();
                                if (floating) {
                                    number->type = token_type::FLOATING;
                                    number->metadata = info_floating{.representation = accumulator.str(), .base = base};
                                } else {
                                    number->type = token_type::INTEGER;
                                    number->metadata = info_integer{.representation = accumulator.str(), .base = base};
                                }
                                result.push_back(number);

                                accumulator.str("");
                                goto skip_reading;
                            }
                        }
                        accumulator << rchar;
                    } while (istream.good());
                }
                    break;
                case '\'':
                    do {
                        rchar = istream.get();

                        if (rchar == '\'') {
                            auto str = std::make_shared<token>();
                            str->type = token_type::STRING;
                            str->metadata = info_string{.content = accumulator.str()};
                            result.push_back(str);

                            accumulator.str("");
                            break;
                        }
                        accumulator << rchar;
                    } while (istream.good());
                    break;
                default: {
                    accumulator << rchar;
                    do {
                        rchar = istream.get();

                        if (!(
                                (rchar >= 'A' && rchar <= 'Z') ||
                                (rchar >= 'a' && rchar <= 'z') ||
                                (rchar >= '0' && rchar <= '9') ||
                                rchar == '_'
                        )) {
                            auto id = std::make_shared<token>();
                            id->type = token_type::IDENTIFIER;
                            id->metadata = info_identifier{.content = accumulator.str()};
                            result.push_back(id);

                            accumulator.str("");
                            goto skip_reading;
                        }
                        accumulator << rchar;
                    } while (istream.good());
                }
            }

            rchar = istream.get();
            skip_reading:;
        } while (istream.good());

        result.push_back(simple_token(token_type::END_OF_FILE));
        D_DEBUG("Lexer finished processing source.", {
            diagnostic::diagnostic_log_marker { .key = "source", .value = name },
            diagnostic::diagnostic_log_marker { .key = "token number", .value = std::to_string(result.size()) }
        });
        D_END_PHASE();
        return result;
    }
}

