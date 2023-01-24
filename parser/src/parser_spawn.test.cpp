#include <gtest/gtest.h>
#include "test-includes/includes.hpp"

using namespace scc::parser::test;
using namespace scc::ast;
using namespace scc::common;

using std::get;

TEST(parser_spawn, spawns_an_entity) {
    const auto result = parse("spawn A()");
    const auto nspawn = child_nth<0, nspawn_entity>(result);

    ASSERT_EQ(nspawn->entity_name, "A");
    ASSERT_TRUE(nspawn->arguments.empty());
}


TEST(parser_spawn, reads_a_spawn_with_a_single_parameter) {
    const auto result = parse("spawn WithANumber(1)");
    const auto fncall = child_nth<0, nspawn_entity>(result);
    const auto entity = fncall->entity_name;
    const auto param = argument_nth<0, nconstant>(fncall);

    ASSERT_EQ(entity, "WithANumber");
    ASSERT_EQ(fncall->arguments.size(), 1);
    ASSERT_EQ(param->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param->content).representation, "1");
}

TEST(parser_spawn, reads_a_spawn_expression_with_multiple_parameters) {
    const auto result = parse("spawn WithMultipleNumbers(1, 2)");
    const auto fncall = child_nth<0, nspawn_entity>(result);
    const auto entity = fncall->entity_name;
    const auto param1 = argument_nth<0, nconstant>(fncall);
    const auto param2 = argument_nth<1, nconstant>(fncall);

    ASSERT_EQ(entity, "WithMultipleNumbers");
    ASSERT_EQ(fncall->arguments.size(), 2);
    ASSERT_EQ(param1->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param1->content).representation, "1");
    ASSERT_EQ(param2->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param2->content).representation, "2");
}

TEST(parser_spawn, reads_a_spawn_expression_with_a_named_argument) {
    const auto result = parse("spawn WithNamedArgument(a=1)");
    const auto fncall = child_nth<0, nspawn_entity>(result);
    const auto entity = fncall->entity_name;
    const auto param1 = argument_nth<0, nfunction_call_named_argument>(fncall);
    const auto param1_value = argument_value<nconstant>(param1);

    ASSERT_EQ(entity, "WithNamedArgument");
    ASSERT_EQ(fncall->arguments.size(), 1);
    ASSERT_EQ(param1->name, "a");
    ASSERT_EQ(param1_value->type, nconstant_type::INTEGER);
    ASSERT_EQ(get<info_integer>(param1_value->content).representation, "1");
}