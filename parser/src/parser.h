#pragma once

#include "node.h"
#include "lexer.h"

namespace scc::parser {
    using namespace scc::ast;
    using namespace scc::lexer;

    class parser {
    public:
        explicit parser();
        ~parser();

        ast_root parse(const token_stream &tokens);
    };
}
