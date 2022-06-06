#pragma once

#include <initializer_list>
#include "pass.h"

namespace scc::passes {
    class pass_manager {
    public:
        explicit pass_manager(
                const std::initializer_list<pass> &validation_passes,
                const std::initializer_list<pass> &mutation_passes
                );

        void run(scc::ast::ast_root &root);
    private:
        const std::initializer_list<pass> &validations;
        const std::initializer_list<pass> &mutations;
    };
}