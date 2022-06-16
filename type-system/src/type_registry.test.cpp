#include <gtest/gtest.h>
#include <variant>

#include "type_registry.h"

TEST(type_registry, types_are_not_registered_by_default) {
    scc::type_system::type_registry reg;
    ASSERT_FALSE(reg.defined("type"));
}

TEST(type_registry, types_are_not_registered_when_resolved_first_time) {
    scc::type_system::type_registry reg;
    reg.resolve("type");
    ASSERT_FALSE(reg.defined("type"));
}


TEST(type_registry, when_a_type_is_resolved_and_does_not_exist_return_fillable_placeholder) {
    scc::type_system::type_registry reg;
    auto type = reg.resolve("type");
    ASSERT_EQ(type->kind, scc::type_system::type_kind::UNKNOWN);
}

TEST(type_registry, types_already_resolve_to_same_backed_instance) {
    scc::type_system::type_registry reg;
    auto type1 = reg.resolve("type");
    auto type2 = reg.resolve("type");

    ASSERT_EQ(type1.get(), type2.get());
}

TEST(type_registry, types_are_marked_as_existing_when_kind_is_not_unknown) {
    scc::type_system::type_registry reg;
    auto type = reg.resolve("type");
    type->kind = scc::type_system::type_kind::VALUE;

    ASSERT_TRUE(reg.defined("type"));
}