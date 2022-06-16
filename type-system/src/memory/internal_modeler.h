#pragma once

#include "../type.h"

namespace scc::type_system::memory {
    class internal_modeler {
    public:
        explicit internal_modeler(const unsigned int goal_cache_size);
        ~internal_modeler();

        virtual void model_type(std::shared_ptr<type> type);
    private:
        const unsigned int goal_cache_size;
    };
}