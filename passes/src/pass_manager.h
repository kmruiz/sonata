#pragma once

#include <initializer_list>
#include "pass.h"
#include "type_registry.h"

namespace scc::passes {
    class pass_manager {
    public:
        explicit pass_manager(std::shared_ptr<scc::type_system::type_registry> &types);

        void run(scc::ast::ast_root &root);
    private:
        const std::initializer_list<std::unique_ptr<pass>> validations;
        const std::initializer_list<std::unique_ptr<pass>> mutations;
    };
}