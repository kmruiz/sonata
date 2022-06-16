#include <gtest/gtest.h>

#include "internal_modeler.h"
#include "../type_registry.h"

using namespace scc::type_system::memory;
using namespace scc::type_system;

TEST(internal_modeler, merges_two_boolean_fields_into_a_bitbag_with_padding) {
    internal_modeler modeler(64);
    type_registry registry;

    auto entity = registry.resolve("example");
    auto boolean = registry.resolve("boolean");

    boolean->kind = type_kind::VALUE;
    entity->kind = type_kind::ENTITY;

    boolean->layout = { .type = layout_type::STATIC, .storages = { { bit_bag { .size = 1, .reservations = { { .bits = 1, .type = bit_bag_reservation_type::BOOLEAN, .translations = { }}}}}}};
    field f1 = { .base_type = boolean, .name = "f1" };
    field f2 = { .base_type = boolean, .name = "f2" };

    entity->fields.emplace_back(std::make_shared<field>(f1));
    entity->fields.emplace_back(std::make_shared<field>(f2));

    modeler.model_type(entity);

    ASSERT_EQ(entity->layout.type, layout_type::STATIC);
}