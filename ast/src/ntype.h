#pragma once

#include "node.h"

namespace scc::ast {
    struct ntype : public node {
        ntype();
        ~ntype();

        string name;
        type_constraints constraints;

        void to_json(json& j) override;
    };
}