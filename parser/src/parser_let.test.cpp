#include <gtest/gtest.h>
#include "test-includes/includes.hpp"

using namespace scc::parser::test;
using namespace scc::ast;
using namespace scc::common;

using std::get;

TEST(parser_let, reads_a_let_expression_with_a_value) {
    const auto result = parse("let x = 1");
    const auto let = child_nth<0, nlet>(result);
    const auto body = body_of<nconstant>(let);

    ASSERT_EQ(let->mutable_p, false);
    ASSERT_EQ(let->name, "x");
    ASSERT_EQ(body->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(body->content).base, numeric_base::DECIMAL);
    ASSERT_EQ(get<info_integer>(body->content).representation, "1");
}

TEST(parser_let, reads_a_mutable_let_expression_with_a_value) {
    const auto result = parse("let mutable x = 0x15");
    const auto let = child_nth<0, nlet>(result);
    const auto body = body_of<nconstant>(let);

    ASSERT_EQ(let->mutable_p, true);
    ASSERT_EQ(let->name, "x");
    ASSERT_EQ(body->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(body->content).base, numeric_base::HEXADECIMAL);
    ASSERT_EQ(get<info_integer>(body->content).representation, "0x15");
}

TEST(parser_let, reads_a_let_expression_with_a_type_expression) {
    const auto result = parse("let withTypeExpr: integer = 0o6");
    const auto let = child_nth<0, nlet>(result);
    const auto body = body_of<nconstant>(let);

    ASSERT_EQ(let->mutable_p, false);
    ASSERT_EQ(let->name, "withTypeExpr");
    ASSERT_EQ(body->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<type_constraint_equality>(let->constraints).type, "integer");
    ASSERT_EQ(get<info_integer>(body->content).base, numeric_base::OCTAL);
    ASSERT_EQ(get<info_integer>(body->content).representation, "0o6");
}

TEST(parser_let, reads_a_mutable_let_expression_without_body) {
    const auto result = parse("let mutable y: integer");
    const auto let = child_nth<0, nlet>(result);

    ASSERT_EQ(let->mutable_p, true);
    ASSERT_EQ(let->name, "y");
    ASSERT_FALSE(has_body(let));
}

TEST(parser_let, reads_a_function_expression_without_parameters_and_without_body) {
    const auto result = parse("let foo(): number");
    const auto let = child_nth<0, nlet_function>(result);

    ASSERT_EQ(let->name, "foo");
    ASSERT_EQ(get<type_constraint_equality>(let->return_type).type, "number");
    ASSERT_FALSE(has_body(let));
}
