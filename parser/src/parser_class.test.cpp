#include <gtest/gtest.h>
#include "test-includes/includes.hpp"

using namespace scc::parser::test;
using namespace scc::ast;
using namespace scc::common;

TEST(parser_class, reads_a_capability_class_without_fields) {
    const auto result = parse("capability class IO()");
    const auto klass = child_nth<0, nclass>(result);

    ASSERT_EQ(klass->type, nclass_type::CAPABILITY);
    ASSERT_EQ(klass->name, "IO");
    ASSERT_EQ(klass->package, "");
    ASSERT_EQ(klass->visibility, nclass_visibility::PUBLIC);
    ASSERT_TRUE(klass->requirements.empty());
    ASSERT_TRUE(klass->fields.empty());
    ASSERT_FALSE(klass->body.has_value());
}

TEST(parser_class, reads_a_capability_class_with_a_field) {
    const auto result = parse("capability class IO(path: String)");
    const auto klass = child_nth<0, nclass>(result);

    ASSERT_EQ(klass->type, nclass_type::CAPABILITY);
    ASSERT_EQ(klass->name, "IO");
    ASSERT_EQ(klass->package, "");
    ASSERT_EQ(klass->visibility, nclass_visibility::PUBLIC);
    ASSERT_TRUE(klass->requirements.empty());
    ASSERT_FALSE(klass->fields.empty());
    ASSERT_FALSE(klass->body.has_value());

    auto path_field = field_nth<0>(klass);
    ASSERT_EQ(path_field->name, "path");
    ASSERT_EQ(get<type_constraint_equality>(path_field->type).type, "String");
}

TEST(parser_class, reads_a_capability_class_with_multiple_field) {
    const auto result = parse("capability class IO(path: String, permissions: Number)");
    const auto klass = child_nth<0, nclass>(result);

    ASSERT_EQ(klass->type, nclass_type::CAPABILITY);
    ASSERT_EQ(klass->name, "IO");
    ASSERT_EQ(klass->package, "");
    ASSERT_EQ(klass->visibility, nclass_visibility::PUBLIC);
    ASSERT_TRUE(klass->requirements.empty());
    ASSERT_FALSE(klass->fields.empty());
    ASSERT_FALSE(klass->body.has_value());

    auto path_field = field_nth<0>(klass);
    ASSERT_EQ(path_field->name, "path");
    ASSERT_EQ(get<type_constraint_equality>(path_field->type).type, "String");

    auto permissions_field = field_nth<1>(klass);
    ASSERT_EQ(permissions_field->name, "permissions");
    ASSERT_EQ(get<type_constraint_equality>(permissions_field->type).type, "Number");
}

TEST(parser_class, reads_a_value_class_with_multiple_field) {
    const auto result = parse("value class Amount(value: Number, currency: String)");
    const auto klass = child_nth<0, nclass>(result);

    ASSERT_EQ(klass->type, nclass_type::VALUE);
    ASSERT_EQ(klass->name, "Amount");
    ASSERT_EQ(klass->package, "");
    ASSERT_EQ(klass->visibility, nclass_visibility::PUBLIC);
    ASSERT_TRUE(klass->requirements.empty());
    ASSERT_FALSE(klass->fields.empty());
    ASSERT_FALSE(klass->body.has_value());

    auto path_field = field_nth<0>(klass);
    ASSERT_EQ(path_field->name, "value");
    ASSERT_EQ(get<type_constraint_equality>(path_field->type).type, "Number");

    auto permissions_field = field_nth<1>(klass);
    ASSERT_EQ(permissions_field->name, "currency");
    ASSERT_EQ(get<type_constraint_equality>(permissions_field->type).type, "String");
}

TEST(parser_class, reads_an_entity_interface_with_a_method) {
    const auto result = parse("entity interface Ping {\n"
                              " let do(): forget\n"
                              "}");
    const auto klass = child_nth<0, nclass>(result);

    ASSERT_EQ(klass->type, nclass_type::ENTITY_INTERFACE);
    ASSERT_EQ(klass->name, "Ping");
    ASSERT_EQ(klass->package, "");
    ASSERT_EQ(klass->visibility, nclass_visibility::PUBLIC);
    ASSERT_TRUE(klass->requirements.empty());
    ASSERT_TRUE(klass->fields.empty());
    ASSERT_TRUE(klass->body.has_value());

    auto fdef = child_nth<0, nlet_function>(klass);
    ASSERT_EQ(fdef->name, "do");
    ASSERT_EQ(get<type_constraint_equality>(fdef->return_type).type, "forget");
}

TEST(parser_class, reads_a_self_set_value) {
    const auto result = parse("self.field = 1");
    const auto setter = child_nth<0, nclass_self_set>(result);

    ASSERT_EQ(setter->field, "field");

    auto nconst = std::dynamic_pointer_cast<nconstant>(setter->value);
    ASSERT_EQ(nconst->type, nconstant_type::INTEGER);
    ASSERT_EQ(std::get<info_integer>(nconst->content).representation, "1");
}
