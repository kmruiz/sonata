#include <gtest/gtest.h>

#include "internal_modeler.h"

using namespace scc::type_system::memory;
using namespace scc::type_system;

TEST(internal_modeler, merges_two_boolean_fields_into_a_bitbag_with_padding) {
    auto registry = std::make_shared<type_registry>();
    internal_modeler modeler(64, registry);

    auto entity = registry->resolve("example_entity");
    auto boolean = registry->resolve("boolean");

    entity->kind = type_kind::ENTITY;

    entity->fields.emplace_back(std::make_shared<field>(field { .base_type = boolean, .name = "f1" }));
    entity->fields.emplace_back(std::make_shared<field>(field { .base_type = boolean, .name = "f2" }));

    modeler.model_type(entity);

    auto bb = std::get<bit_bag>(entity->layout.storages[0]);
    ASSERT_EQ(entity->layout.type, layout_type::STATIC);

    ASSERT_EQ(bb.size, 8);
    ASSERT_EQ(bb.reservations[0].bits, 1);
    ASSERT_EQ(bb.reservations[0].type, bit_bag_reservation_type::BOOLEAN);
    ASSERT_EQ(bb.reservations[1].bits, 1);
    ASSERT_EQ(bb.reservations[1].type, bit_bag_reservation_type::BOOLEAN);
    ASSERT_EQ(entity->fields[0]->selector.type, selector_type::BIT_BAG);
    ASSERT_EQ(entity->fields[0]->selector.offset, 0);
    ASSERT_EQ(entity->fields[1]->selector.type, selector_type::BIT_BAG);
    ASSERT_EQ(entity->fields[1]->selector.offset, 1);
}

//TEST(internal_modeler, merges_value_classes_with_booleans_into_parent) {
//    auto registry = std::make_shared<type_registry>();
//    internal_modeler modeler(64, registry);
//
//    auto entity = registry->resolve("example");
//    auto vc = registry->resolve("valueclass");
//    auto boolean = registry->resolve("boolean");
//
//    entity->kind = type_kind::ENTITY;
//    vc->kind = type_kind::VALUE;
//    vc->fields.emplace_back(std::make_shared<field>(field { .base_type = boolean, .name = "field" }));
//    entity->fields.emplace_back(std::make_shared<field>(field { .base_type = vc, .name = "vc1" }));
//    entity->fields.emplace_back(std::make_shared<field>(field { .base_type = vc, .name = "vc2" }));
//    modeler.model_type(entity);
//
//    ASSERT_EQ(entity->layout.type, layout_type::STATIC);
//
//    auto bb = std::get<bit_bag>(entity->layout.storages[0]);
//    ASSERT_EQ(bb.size, 8);
//    ASSERT_EQ(bb.reservations[0].bits, 1);
//    ASSERT_EQ(bb.reservations[0].type, bit_bag_reservation_type::BOOLEAN);
//    ASSERT_EQ(bb.reservations[1].bits, 1);
//    ASSERT_EQ(bb.reservations[1].type, bit_bag_reservation_type::BOOLEAN);
//    ASSERT_EQ(entity->fields[0]->selector.type, selector_type::BIT_BAG);
//    ASSERT_EQ(entity->fields[0]->selector.offset, 0);
//    ASSERT_EQ(entity->fields[1]->selector.type, selector_type::BIT_BAG);
//    ASSERT_EQ(entity->fields[1]->selector.offset, 1);
//}

TEST(internal_modeler, merges_integers_and_bits) {
    auto registry = std::make_shared<type_registry>();
    internal_modeler modeler(64, registry);

    auto entity = registry->resolve("example");
    auto vc = registry->resolve("valueclass");
    auto boolean = registry->resolve("boolean");
    auto integer = registry->resolve("integer");

    entity->kind = type_kind::ENTITY;
    vc->kind = type_kind::VALUE;
    vc->fields.emplace_back(std::make_shared<field>(field { .base_type = boolean, .name = "field" }));
    entity->fields.emplace_back(std::make_shared<field>(field { .base_type = vc, .name = "vc" }));
    entity->fields.emplace_back(std::make_shared<field>(field { .base_type = integer, .name = "int" }));
    modeler.model_type(entity);

    ASSERT_EQ(entity->layout.type, layout_type::STATIC);

    auto direct = std::get<direct_mapping>(entity->layout.storages[0]);
    auto bb = std::get<bit_bag>(entity->layout.storages[1]);
    auto pad = std::get<padding>(entity->layout.storages[2]);
    ASSERT_EQ(bb.size, 8);
    ASSERT_EQ(bb.reservations[0].bits, 1);
    ASSERT_EQ(bb.reservations[0].type, bit_bag_reservation_type::BOOLEAN);
    ASSERT_EQ(direct.size, 32);
    ASSERT_EQ(pad.size, 64 - 32 - 8);
}