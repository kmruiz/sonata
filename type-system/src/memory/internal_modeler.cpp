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

    }

    internal_modeler::~internal_modeler() = default;

    void internal_modeler::model_type(std::shared_ptr<type> &type) {
        type->layout.type = layout_type::STATIC;

        auto current_bitbag = bit_bag { .size = BYTE_SIZE, .reservations = {} };
        auto remaining_from_bitbag = BYTE_SIZE;

        for (auto &field : type->fields) {
            auto field_type = field->base_type;
            if (field_type->layout.type == layout_type::NONE) {
                model_type(field_type);
            }

            merge_into_parent_bit_bag(type, current_bitbag, remaining_from_bitbag, field_type);
        }

        if (!current_bitbag.reservations.empty()) {
            type->layout.storages.emplace_back(current_bitbag);
        }

        pad_to_cacheable(type);
    }

    void internal_modeler::merge_into_parent_bit_bag(std::shared_ptr<type> &root, bit_bag &current_bitbag, unsigned int &remaining_from_bitbag, const std::shared_ptr<type> &field_type) const {
        if (field_type->kind == type_kind::PRIMITIVE) {
            // primitives can be easily merged because they are always static and have a fixed size
            if (field_type == boolean_type) {
                remaining_from_bitbag -= 1;
                current_bitbag.reservations.emplace_back(bit_bag_reservation { .bits = 1, .type = bit_bag_reservation_type::BOOLEAN });
            }

            if (field_type == byte_type || field_type == short_type || field_type == integer_type || field_type == long_type) {
                root->layout.storages.insert(root->layout.storages.end(), field_type->layout.storages.begin(),field_type->layout.storages.end());
            }

        } else if (field_type->kind == type_kind::VALUE) {
            for (const auto& field : field_type->fields) {
                merge_into_parent_bit_bag(root, current_bitbag, remaining_from_bitbag, field->base_type);
            }
        }
    }

    void internal_modeler::pad_to_cacheable(std::shared_ptr<type> &root) {
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
    }
}