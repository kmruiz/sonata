#pragma once

#include <initializer_list>
#include "pass.h"

namespace scc::passes {
    class pass_manager {
    public:
        explicit pass_manager();

        void run(scc::ast::ast_root &root);
    private:
        const std::initializer_list<pass> validations;
        const std::initializer_list<pass> mutations;
    };
}