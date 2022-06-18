#pragma once

#include "../type_registry.h"
#include "../type.h"

namespace scc::type_system::memory {
    class internal_modeler {
    public:
        explicit internal_modeler(const unsigned int goal_cache_size, std::shared_ptr<type_registry> &types);
        ~internal_modeler();

        void model_all_types() const;
        void model_type(std::shared_ptr<type> &type) const;
    private:
        const unsigned int goal_cache_size;
        std::shared_ptr<type_registry> types;

        std::shared_ptr<type> boolean_type;
        std::shared_ptr<type> byte_type;
        std::shared_ptr<type> short_type;
        std::shared_ptr<type> integer_type;
        std::shared_ptr<type> long_type;
        std::shared_ptr<type> floating_type;
        std::shared_ptr<type> double_type;

        void merge_into_parent_bit_bag(std::shared_ptr<type> &root, bit_bag &current_bitbag, unsigned int &remaining_from_bitbag, const std::shared_ptr<field> &field) const;
        void pad_to_cacheable(std::shared_ptr<type> &root) const;
    };
}