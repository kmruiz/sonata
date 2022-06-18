#pragma once

#include "diagnostic.h"
#include "ast.h"

namespace scc::passes {
    class pass {
    public:
        virtual ~pass() = default;
        virtual void execute(scc::ast::ast_root &root) const = 0;
        virtual diagnostic::diagnostic_phase_id pass_phase() const = 0;
    };
}