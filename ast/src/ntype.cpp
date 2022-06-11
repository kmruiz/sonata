#include "ntype.h"

namespace scc::ast {
    ntype::ntype() = default;
    ntype::~ntype() = default;

    void ntype::to_json(json &j) {
        j["name"] = this->name;
        j["constraints"] = this->constraints;
    }
}