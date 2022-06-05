#include "nidentifier.h"


namespace scc::ast {
    ast::nidentifier::nidentifier() = default;
    ast::nidentifier::~nidentifier() = default;

    void nidentifier::to_json(json &j) {
        j["name"] = name;
    }
}