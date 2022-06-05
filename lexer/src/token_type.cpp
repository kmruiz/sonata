#include "lexer.h"

namespace scc::lexer {
    std::string to_string(const token_type &type) {
        switch (type) {
            case token_type::COMMENT:
                return "COMMENT";
            case token_type::WHITESPACE:
                return "WHITESPACE";
            case token_type::NEW_LINE:
                return "NEW_LINE";
            case token_type::END_OF_FILE:
                return "END_OF_FILE";

            case token_type::IDENTIFIER:
                return "IDENTIFIER";
            case token_type::INTEGER:
                return "INTEGER";
            case token_type::FLOATING:
                return "FLOATING";

            case token_type::COLON:
                return "COLON";
            case token_type::COMMA:
                return "COMMA";
            case token_type::DOT:
                return "DOT";

            case token_type::OPEN_PAREN:
                return "OPEN_PAREN";
            case token_type::OPEN_BRACKET:
                return "OPEN_BRACKET";
            case token_type::OPEN_BRACE:
                return "OPEN_BRACE";
            case token_type::CLOSE_PAREN:
                return "CLOSE_PAREN";
            case token_type::CLOSE_BRACKET:
                return "CLOSE_BRACKET";
            case token_type::CLOSE_BRACE:
                return "CLOSE_BRACE";
            case token_type::STRING:
                return "STRING";

            case token_type::AT:
                return "AT";
            case token_type::HASH:
                return "HASH";
            case token_type::TILDE:
                return "TILDE";
            case token_type::QUESTION_MARK:
                return "QUESTION_MARK";
            case token_type::EXCLAMATION_MARK:
                return "EXCLAMATION_MARK";
            case token_type::EQUALS:
                return "EQUALS";
            case token_type::LESS_THAN:
                return "LESS_THAN";
            case token_type::GREATER_THAN:
                return "GREATER_THAN";
            case token_type::PLUS:
                return "PLUS";
            case token_type::MINUS:
                return "MINUS";
            case token_type::MULTIPLY:
                return "MULTIPLY";
            case token_type::DIVIDE:
                return "DIVIDE";
            case token_type::CARET:
                return "CARET";
            case token_type::PERCENT:
                return "PERCENT";
            case token_type::AMPERSAND:
                return "AMPERSAND";
            case token_type::UNKNOWN:
                return "UNKNOWN";
        }

        return "UNKNOWN";
    }
}