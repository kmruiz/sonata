#pragma once

#include <string>
#include <vector>
#include <memory>
#include <tuple>
#include <variant>

namespace scc::type_system {
    namespace memory {
        enum class layout_type : unsigned char {
            STATIC, FLEX
        };

        enum class bit_bag_reservation_type : unsigned char {
            BOOLEAN, ENUM
        };

        struct big_bag_reservation_enum_translation {
            unsigned int enum_value;
            unsigned int bit;
            bool value;
        };

        struct bit_bag_reservation {
            unsigned int bits;
            bit_bag_reservation_type type;
            std::vector<big_bag_reservation_enum_translation> translations;
        };

        struct bit_bag {
            unsigned int size;
            std::vector<bit_bag_reservation> reservations;
        };

        struct direct_mapping {
            unsigned int size;
        };

        struct reference {
        };

        struct padding {
            unsigned int bits;
        };

        typedef std::variant<bit_bag, direct_mapping, reference, padding> memory_storage;

        struct layout {
            layout_type type;
            std::vector<memory_storage> storages;
        };
    }

    enum class type_kind : unsigned char {
        UNKNOWN, VALUE, ENTITY, CAPABILITY
    };

    struct type;
    struct field;
    struct method;

    struct type {
        type_kind kind;
        bool is_clustered;
        std::shared_ptr<type> parent;
        memory::layout layout;
        std::vector<std::shared_ptr<field>> fields;
        std::vector<std::shared_ptr<method>> methods;
    };

    struct field {
        std::shared_ptr<type> base_type;
        std::string name;
    };

    struct method {
        std::shared_ptr<type> return_type;
        std::vector<std::tuple<std::string, std::shared_ptr<type>>> parameters_types;
        std::string name;
    };
}