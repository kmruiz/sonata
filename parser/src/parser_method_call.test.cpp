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