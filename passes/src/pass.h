#pragma once

#include "ast.h"

namespace scc::passes {
    class pass {
    public:
        virtual ~pass() = 0;
        virtual void execute(scc::ast::ast_root &root) = 0;
    };
}