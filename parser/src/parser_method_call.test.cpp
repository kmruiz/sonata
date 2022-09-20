#include <gtest/gtest.h>
#include "test-includes/includes.hpp"

using namespace scc::parser::test;
using namespace scc::ast;
using namespace scc::common;

using std::get;

TEST(parser_method_call, reads_a_method_call_from_a_variable) {
    const auto result = parse("a.b()");
    const auto mcall = child_nth<0, nmethod_call>(result);
    const auto left = left_op<nidentifier>(mcall);

    ASSERT_EQ(left->name, "a");
    ASSERT_EQ(mcall->method, "b");
    ASSERT_TRUE(mcall->arguments.empty());
}

TEST(parser_method_call, reads_a_method_call_from_a_variable_with_parameters) {
    const auto result = parse("a.b(1)");
    const auto mcall = child_nth<0, nmethod_call>(result);
    const auto left = left_op<nidentifier>(mcall);

    ASSERT_EQ(left->name, "a");
    ASSERT_EQ(mcall->method, "b");

    auto first_arg = argument_nth<0, nconstant>(mcall);
    ASSERT_EQ(first_arg->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(first_arg->content).representation, "1");
}

TEST(parser_method_call, reads_a_method_call_from_a_variable_with_named_parameters) {
    const auto result = parse("a.b(t=42)");
    const auto mcall = child_nth<0, nmethod_call>(result);
    const auto left = left_op<nidentifier>(mcall);

    ASSERT_EQ(left->name, "a");
    ASSERT_EQ(mcall->method, "b");

    auto first_arg_named = argument_nth<0, nfunction_call_named_argument>(mcall);
    ASSERT_EQ(first_arg_named->name, "t");

    auto first_arg = argument_value<nconstant>(first_arg_named);
    ASSERT_EQ(get<info_integer>(first_arg->content).representation, "42");
}