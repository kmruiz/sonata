#include <gtest/gtest.h>
#include "test-includes/includes.hpp"

using namespace scc::parser::test;
using namespace scc::ast;
using namespace scc::common;

using std::get;

TEST(parser_function_call, reads_a_function_call_without_parameters) {
    const auto result = parse("withNoParams()");
    const auto fncall = child_nth<0, nfunction_call>(result);
    const auto left = left_op<nidentifier>(fncall);

    ASSERT_EQ(left->name, "withNoParams");
    ASSERT_TRUE(fncall->arguments.empty());
}

TEST(parser_function_call, reads_a_function_call_with_a_single_parameter) {
    const auto result = parse("withANumber(1)");
    const auto fncall = child_nth<0, nfunction_call>(result);
    const auto left = left_op<nidentifier>(fncall);
    const auto param = argument_nth<0, nconstant>(fncall);

    ASSERT_EQ(left->name, "withANumber");
    ASSERT_EQ(fncall->arguments.size(), 1);
    ASSERT_EQ(param->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param->content).representation, "1");
}

TEST(parser_function_call, reads_a_function_call_with_multiple_parameters) {
    const auto result = parse("withNumbers(1, 2.5)");
    const auto fncall = child_nth<0, nfunction_call>(result);
    const auto left = left_op<nidentifier>(fncall);
    const auto param1 = argument_nth<0, nconstant>(fncall);
    const auto param2 = argument_nth<1, nconstant>(fncall);

    ASSERT_EQ(left->name, "withNumbers");
    ASSERT_EQ(fncall->arguments.size(), 2);
    ASSERT_EQ(param1->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param1->content).representation, "1");
    ASSERT_EQ(param2->type, nconstant_type::FLOATING);
    ASSERT_EQ(get<info_floating>(param2->content).representation, "2.5");
}

TEST(parser_function_call, reads_a_function_call_with_a_named_argument) {
    const auto result = parse("withNamedArgument(a=1)");
    const auto fncall = child_nth<0, nfunction_call>(result);
    const auto left = left_op<nidentifier>(fncall);
    const auto param1 = argument_nth<0, nfunction_call_named_argument>(fncall);
    const auto param1_value = argument_value<nconstant>(param1);

    ASSERT_EQ(left->name, "withNamedArgument");
    ASSERT_EQ(fncall->arguments.size(), 1);
    ASSERT_EQ(param1->name, "a");
    ASSERT_EQ(param1_value->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param1_value->content).representation, "1");
}

TEST(parser_function_call, reads_a_function_call_with_multiple_named_arguments) {
    const auto result = parse("withMultipleNamedArguments(a=1, b='hey')");
    const auto fncall = child_nth<0, nfunction_call>(result);
    const auto left = left_op<nidentifier>(fncall);
    const auto param1 = argument_nth<0, nfunction_call_named_argument>(fncall);
    const auto param1_value = argument_value<nconstant>(param1);
    const auto param2 = argument_nth<1, nfunction_call_named_argument>(fncall);
    const auto param2_value = argument_value<nconstant>(param2);

    ASSERT_EQ(left->name, "withMultipleNamedArguments");
    ASSERT_EQ(fncall->arguments.size(), 2);
    ASSERT_EQ(param1->name, "a");
    ASSERT_EQ(param1_value->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param1_value->content).representation, "1");
    ASSERT_EQ(param2->name, "b");
    ASSERT_EQ(param2_value->type, nconstant_type::STRING);
    ASSERT_EQ(get<info_string>(param2_value->content).content, "hey");
}