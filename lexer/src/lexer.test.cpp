#include <gtest/gtest.h>
#include <sstream>
#include <variant>

#include "lexer.h"

#define LEXER_PROCESS(var, code) \
    std::stringstream test(code);         \
    auto lexer = scc::lexer::lexer();      \
    auto var = lexer.process(test, testing::UnitTest::GetInstance()->current_test_info()->name())

#define CONSUME_TOKEN(var, result) \
    auto var = result.front();     \
    result.pop_front()

#define ASSERT_NEXT_TOKEN(_type) do { \
    auto assertx = result.front();     \
    result.pop_front();         \
    ASSERT_EQ(assertx->type, _type); \
} while (0)

TEST(lexer, reads_a_comment_from_a_stream) {
    LEXER_PROCESS(result, "; this is a comment\n");

    CONSUME_TOKEN(comment, result);
    ASSERT_EQ(comment->type, scc::lexer::token_type::COMMENT);
    ASSERT_EQ(std::get<scc::lexer::info_comment>(comment->metadata).content, "; this is a comment");

    CONSUME_TOKEN(new_line, result);
    ASSERT_EQ(new_line->type, scc::lexer::token_type::NEW_LINE);
}

TEST(lexer, reads_whitespaces) {
    LEXER_PROCESS(result, " \t");

    CONSUME_TOKEN(whitespace, result);
    ASSERT_EQ(whitespace->type, scc::lexer::token_type::WHITESPACE);

    CONSUME_TOKEN(tab, result);
    ASSERT_EQ(tab->type, scc::lexer::token_type::WHITESPACE);
}

TEST(lexer, reads_identifiers) {
    LEXER_PROCESS(result, "identifier _identifier identifier1337 ");

    CONSUME_TOKEN(first, result);
    ASSERT_EQ(first->type, scc::lexer::token_type::IDENTIFIER);
    ASSERT_EQ(std::get<scc::lexer::info_identifier>(first->metadata).content, "identifier");
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);

    CONSUME_TOKEN(second, result);
    ASSERT_EQ(second->type, scc::lexer::token_type::IDENTIFIER);
    ASSERT_EQ(std::get<scc::lexer::info_identifier>(second->metadata).content, "_identifier");
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);

    CONSUME_TOKEN(third, result);
    ASSERT_EQ(third->type, scc::lexer::token_type::IDENTIFIER);
    ASSERT_EQ(std::get<scc::lexer::info_identifier>(third->metadata).content, "identifier1337");
}

TEST(lexer, reads_integers) {
    LEXER_PROCESS(result, "50 0x50 0o50 0b10 ");

    CONSUME_TOKEN(fifty10, result);
    ASSERT_EQ(fifty10->type, scc::lexer::token_type::INTEGER);
    ASSERT_EQ(std::get<scc::lexer::info_integer>(fifty10->metadata).representation, "50");
    ASSERT_EQ(std::get<scc::lexer::info_integer>(fifty10->metadata).base,
              scc::lexer::numeric_base::DECIMAL);
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);

    CONSUME_TOKEN(fifty16, result);
    ASSERT_EQ(fifty16->type, scc::lexer::token_type::INTEGER);
    ASSERT_EQ(std::get<scc::lexer::info_integer>(fifty16->metadata).representation, "0x50");
    ASSERT_EQ(std::get<scc::lexer::info_integer>(fifty16->metadata).base,
              scc::lexer::numeric_base::HEXADECIMAL);
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);

    CONSUME_TOKEN(fifty8, result);
    ASSERT_EQ(fifty8->type, scc::lexer::token_type::INTEGER);
    ASSERT_EQ(std::get<scc::lexer::info_integer>(fifty8->metadata).representation, "0o50");
    ASSERT_EQ(std::get<scc::lexer::info_integer>(fifty8->metadata).base,
              scc::lexer::numeric_base::OCTAL);
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);

    CONSUME_TOKEN(ten2, result);
    ASSERT_EQ(ten2->type, scc::lexer::token_type::INTEGER);
    ASSERT_EQ(std::get<scc::lexer::info_integer>(ten2->metadata).representation, "0b10");
    ASSERT_EQ(std::get<scc::lexer::info_integer>(ten2->metadata).base,
              scc::lexer::numeric_base::BINARY);
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);
}

TEST(lexer, reads_floating_point_decimals) {
    LEXER_PROCESS(result, "50.5 ");

    CONSUME_TOKEN(fifty10, result);
    ASSERT_EQ(fifty10->type, scc::lexer::token_type::FLOATING);
    ASSERT_EQ(std::get<scc::lexer::info_floating>(fifty10->metadata).representation, "50.5");
    ASSERT_EQ(std::get<scc::lexer::info_floating>(fifty10->metadata).base,
              scc::lexer::numeric_base::DECIMAL);
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);
}

TEST(lexer, reads_floating_point_decimals_after_a_comma) {
    LEXER_PROCESS(result, ", 50.5 ");

    CONSUME_TOKEN(comma, result);
    CONSUME_TOKEN(whitespace, result);

    CONSUME_TOKEN(fifty10, result);
    ASSERT_EQ(fifty10->type, scc::lexer::token_type::FLOATING);
    ASSERT_EQ(std::get<scc::lexer::info_floating>(fifty10->metadata).representation, "50.5");
    ASSERT_EQ(std::get<scc::lexer::info_floating>(fifty10->metadata).base,
              scc::lexer::numeric_base::DECIMAL);
    ASSERT_NEXT_TOKEN(scc::lexer::token_type::WHITESPACE);
}

TEST(lexer, reads_parens_brackets_and_braces) {
    LEXER_PROCESS(result, "()[]{} ");

    CONSUME_TOKEN(oparen, result);
    ASSERT_EQ(oparen->type, scc::lexer::token_type::OPEN_PAREN);

    CONSUME_TOKEN(cparen, result);
    ASSERT_EQ(cparen->type, scc::lexer::token_type::CLOSE_PAREN);

    CONSUME_TOKEN(obracket, result);
    ASSERT_EQ(obracket->type, scc::lexer::token_type::OPEN_BRACKET);

    CONSUME_TOKEN(cbracket, result);
    ASSERT_EQ(cbracket->type, scc::lexer::token_type::CLOSE_BRACKET);

    CONSUME_TOKEN(obrace, result);
    ASSERT_EQ(obrace->type, scc::lexer::token_type::OPEN_BRACE);

    CONSUME_TOKEN(cbrace, result);
    ASSERT_EQ(cbrace->type, scc::lexer::token_type::CLOSE_BRACE);
}

TEST(lexer, reads_separators) {
    LEXER_PROCESS(result, ",:. ");

    CONSUME_TOKEN(comma, result);
    ASSERT_EQ(comma->type, scc::lexer::token_type::COMMA);

    CONSUME_TOKEN(colon, result);
    ASSERT_EQ(colon->type, scc::lexer::token_type::COLON);

    CONSUME_TOKEN(dot, result);
    ASSERT_EQ(dot->type, scc::lexer::token_type::DOT);
}

TEST(lexer, reads_operators) {
    LEXER_PROCESS(result, "@#~?!=<>+-*/^%& ");

    CONSUME_TOKEN(at, result);
    ASSERT_EQ(at->type, scc::lexer::token_type::AT);

    CONSUME_TOKEN(hash, result);
    ASSERT_EQ(hash->type, scc::lexer::token_type::HASH);

    CONSUME_TOKEN(tilde, result);
    ASSERT_EQ(tilde->type, scc::lexer::token_type::TILDE);

    CONSUME_TOKEN(qmark, result);
    ASSERT_EQ(qmark->type, scc::lexer::token_type::QUESTION_MARK);

    CONSUME_TOKEN(emark, result);
    ASSERT_EQ(emark->type, scc::lexer::token_type::EXCLAMATION_MARK);

    CONSUME_TOKEN(equals, result);
    ASSERT_EQ(equals->type, scc::lexer::token_type::EQUALS);

    CONSUME_TOKEN(lt, result);
    ASSERT_EQ(lt->type, scc::lexer::token_type::LESS_THAN);

    CONSUME_TOKEN(gt, result);
    ASSERT_EQ(gt->type, scc::lexer::token_type::GREATER_THAN);

    CONSUME_TOKEN(plus, result);
    ASSERT_EQ(plus->type, scc::lexer::token_type::PLUS);

    CONSUME_TOKEN(minus, result);
    ASSERT_EQ(minus->type, scc::lexer::token_type::MINUS);

    CONSUME_TOKEN(multiply, result);
    ASSERT_EQ(multiply->type, scc::lexer::token_type::MULTIPLY);

    CONSUME_TOKEN(divide, result);
    ASSERT_EQ(divide->type, scc::lexer::token_type::DIVIDE);

    CONSUME_TOKEN(caret, result);
    ASSERT_EQ(caret->type, scc::lexer::token_type::CARET);

    CONSUME_TOKEN(percent, result);
    ASSERT_EQ(percent->type, scc::lexer::token_type::PERCENT);

    CONSUME_TOKEN(ampersand, result);
    ASSERT_EQ(ampersand->type, scc::lexer::token_type::AMPERSAND);
}

TEST(lexer, reads_quotes) {
    LEXER_PROCESS(result, "'abc'");

    CONSUME_TOKEN(str, result);
    ASSERT_EQ(str->type, scc::lexer::token_type::STRING);
    ASSERT_EQ(std::get<scc::lexer::info_string>(str->metadata).content, "abc");
}