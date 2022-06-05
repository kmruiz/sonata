#pragma once

namespace scc::lexer {
    enum class token_type : unsigned char {
        COMMENT,
        WHITESPACE,
        NEW_LINE,
        END_OF_FILE,

        IDENTIFIER,
        INTEGER,
        FLOATING,

        COLON,
        COMMA,
        DOT,

        OPEN_PAREN,
        OPEN_BRACKET,
        OPEN_BRACE,
        CLOSE_PAREN,
        CLOSE_BRACKET,
        CLOSE_BRACE,
        STRING,

        AT,
        HASH,
        TILDE,
        QUESTION_MARK,
        EXCLAMATION_MARK,
        EQUALS,
        LESS_THAN,
        GREATER_THAN,
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        CARET,
        PERCENT,
        AMPERSAND,

        UNKNOWN
    };

    std::string to_string(const token_type &type);
}