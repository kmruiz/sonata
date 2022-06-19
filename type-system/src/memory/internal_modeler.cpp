#include "internal_modeler.h"

#include <variant>

const unsigned int BYTE_SIZE = 8;
const unsigned int POINTER_SIZE = 64;

namespace scc::type_system::memory {
    internal_modeler::internal_modeler(const unsigned int goal_cache_size, std::shared_ptr<type_registry> &types)
        : goal_cache_size(goal_cache_size), types(types) {
        boolean_type = types->resolve("boolean");
        boolean_type->kind = type_kind::PRIMITIVE;
        boolean_type->layout = { .type = layout_type::STATIC, .storages = { { bit_bag { .size = 1, .reservations = { { .bits = 1, .type = bit_bag_reservation_type::BOOLEAN, .translations = { }}}}}}};

        byte_type = types->resolve("byte");
        byte_type->kind = type_kind::PRIMITIVE;
        byte_type->layout = { .type = layout_type::STATIC, .storages = { { direct_mapping { .size = BYTE_SIZE } } } };

        short_type = types->resolve("short");
        short_type->kind = type_kind::PRIMITIVE;
        short_type->layout = { .type = layout_type::STATIC, .storages = { { direct_mapping { .size = BYTE_SIZE * 2 } } } };

        integer_type = types->resolve("integer");
        integer_type->kind = type_kind::PRIMITIVE;
        integer_type->layout = { .type = layout_type::STATIC, .storages = { { direct_mapping { .size = BYTE_SIZE * 4 } } } };

        long_type = types->resolve("long");
        long_type->kind = type_kind::PRIMITIVE;
        long_type->layout = { .type = layout_type::STATIC, .storages = { { direct_mapping { .size = BYTE_SIZE * 8 } } } };

        floating_type = types->resolve("float");
        floating_type->kind = type_kind::PRIMITIVE;
        floating_type->layout = { .type = layout_type::STATIC, .storages = { { direct_mapping { .size = BYTE_SIZE * 4 } } } };

        double_type = types->resolve("double");
        double_type->kind = type_kind::PRIMITIVE;
        double_type->layout = { .type = layout_type::STATIC, .storages = { { direct_mapping { .size = BYTE_SIZE * 8 } } } };

    }

    internal_modeler::~internal_modeler() = default;

    void internal_modeler::model_type(std::shared_ptr<type> &type) const {
        type->layout.type = layout_type::STATIC;

        unsigned int offset = 0;
        auto current_bitbag = bit_bag { .size = BYTE_SIZE, .reservations = {} };
        auto remaining_from_bitbag = BYTE_SIZE;

        for (auto &field : type->fields) {
            merge_into_parent(type, offset, current_bitbag, remaining_from_bitbag, field);
        }

        if (!current_bitbag.reservations.empty()) {
            type->layout.storages.emplace_back(current_bitbag);
        }

        pad_to_cacheable(type);
    }

    void internal_modeler::merge_into_parent(std::shared_ptr<type> &root, unsigned int &offset, bit_bag &current_bitbag, unsigned int &remaining_from_bitbag, const std::shared_ptr<field> &field) const {
        const auto field_type = field->base_type;

        if (field_type->kind == type_kind::PRIMITIVE) {
            // primitives can be easily merged because they are always static and have a fixed size
            if (field_type == boolean_type) {
                field->selector = selector { .type = selector_type::BIT_BAG, .offset = current_bitbag.size - remaining_from_bitbag };
                current_bitbag.reservations.emplace_back(bit_bag_reservation { .bits = 1, .type = bit_bag_reservation_type::BOOLEAN });

                remaining_from_bitbag--;
            }

            if (field_type == byte_type || field_type == short_type || field_type == integer_type || field_type == long_type || field_type == floating_type || field_type == double_type) {
                field->selector = selector { .type = selector_type::DIRECT, .offset = offset++ };
                root->layout.storages.insert(root->layout.storages.end(), field_type->layout.storages.begin(),field_type->layout.storages.end());
            }

        } else if (field_type->kind == type_kind::VALUE) {
            for (const auto& inner_field : field_type->fields) {
                merge_into_parent(root, offset, current_bitbag, remaining_from_bitbag, inner_field);
            }
        }
    }

    void internal_modeler::pad_to_cacheable(std::shared_ptr<type> &root) const {
        unsigned int size = 0;

        for (auto &storage : root->layout.storages) {
            if (std::holds_alternative<bit_bag>(storage)) {
                size += std::get<bit_bag>(storage).size;
            }

            if (std::holds_alternative<direct_mapping>(storage)) {
                size += std::get<direct_mapping>(storage).size;
            }

            if (std::holds_alternative<reference>(storage)) {
                size += POINTER_SIZE;
            }

            if (std::holds_alternative<padding>(storage)) {
                size += std::get<padding>(storage).size;
            }
        }

        unsigned int pad = 0;
        if (size == 0) {
            return;
        }

        if (size > goal_cache_size) {
            pad += size % goal_cache_size;
        } else {
            if (size < goal_cache_size / 2) {
                pad += ((goal_cache_size / 2) % size);
            } else {
                pad += (goal_cache_size % size);
            }
        }

        if (pad > 0) {
            root->layout.storages.emplace_back(padding { .size = pad });
        }

        size += pad;
        root->layout.size_in_bytes = size / 8;
    }

    void internal_modeler::model_all_types() const {
        for (auto &t : types->all_types()) {
            model_type(t);
        }
    }
}