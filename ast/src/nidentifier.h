#pragma once

#include <variant>
#include <string>

#include "metadata.h"
#include "node.h"

namespace scc::ast {
    using std::variant;
    using namespace scc::common;

    struct nidentifier : public expression {
        nidentifier();
        ~nidentifier();

        string name;

        void to_json(json& j) override;
    };
}