#pragma once

#include <string>
#include <vector>
#include <memory>
#include <tuple>
#include <variant>

namespace scc::type_system {
    namespace memory {
        enum class layout_type : unsigned char {
            NONE, STATIC, FLEXIBLE
        };

        enum class bit_bag_reservation_type : unsigned char {
            BOOLEAN, ENUM
        };

        enum class selector_type : unsigned char {
            BIT_BAG, DIRECT
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
            unsigned int size;
        };

        typedef std::variant<bit_bag, direct_mapping, reference, padding> memory_storage;

        struct layout {
            layout_type type;
            std::vector<memory_storage> storages;
        };

        struct selector {
            selector_type type;
            unsigned int offset;
        };
    }

    enum class type_kind : unsigned char {
        UNKNOWN, PRIMITIVE, VALUE, ENTITY, CAPABILITY
    };

    struct type;
    struct field;
    struct method;

    struct type {
        type_kind kind;
        std::string name;
        std::shared_ptr<type> parent;
        memory::layout layout;
        std::vector<std::shared_ptr<field>> fields;
        std::vector<std::shared_ptr<method>> methods;
    };

    struct field {
        std::shared_ptr<type> base_type;
        std::string name;
        memory::selector selector;
    };

    struct method {
        std::shared_ptr<type> return_type;
        std::vector<std::tuple<std::string, std::shared_ptr<type>>> parameters_types;
        std::string name;
    };
}