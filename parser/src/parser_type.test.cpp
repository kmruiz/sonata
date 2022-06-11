#include <gtest/gtest.h>
#include "test-includes/includes.hpp"

using namespace scc::parser::test;
using namespace scc::ast;
using namespace scc::common;

using std::get;

TEST(parser_type, alias_a_type) {
    const auto result = parse("type text = string");
    const auto type = child_nth<0, ntype>(result);

    ASSERT_EQ(type->name, "text");
    ASSERT_EQ(get<type_constraint_equality>(type->constraints).type, "string");
}

TEST(parser_type, alias_a_generic_type) {
    const auto result = parse("type ints = list[int]");
    const auto type = child_nth<0, ntype>(result);

    ASSERT_EQ(type->name, "ints");
    ASSERT_EQ(get<type_constraint_generic>(type->constraints).base, "list");
    auto genpar0 = generic_parameter_nth<0, type_constraint_equality>(type->constraints);
    ASSERT_EQ(genpar0.type, "int");
}

TEST(parser_type, defines_a_constant_string_type) {
    const auto result = parse("type undefined = 'undefined'");
    const auto type = child_nth<0, ntype>(result);

    ASSERT_EQ(type->name, "undefined");
    ASSERT_EQ(get<type_constraint_constant>(type->constraints).value, "undefined");
    ASSERT_EQ(get<type_constraint_constant>(type->constraints).base_constraint.type, "string");
}


TEST(parser_type, defines_a_constant_floating_type) {
    const auto result = parse("type pi = 3.14");
    const auto type = child_nth<0, ntype>(result);

    ASSERT_EQ(type->name, "pi");
    ASSERT_EQ(get<type_constraint_constant>(type->constraints).value, "3.14");
    ASSERT_EQ(get<type_constraint_constant>(type->constraints).base_constraint.type, "number");
}

TEST(parser_type, defines_a_constant_integer_type) {
    const auto result = parse("type ten = 10");
    const auto type = child_nth<0, ntype>(result);

    ASSERT_EQ(type->name, "ten");
    ASSERT_EQ(get<type_constraint_constant>(type->constraints).value, "10");
    ASSERT_EQ(get<type_constraint_constant>(type->constraints).base_constraint.type, "number");
}

