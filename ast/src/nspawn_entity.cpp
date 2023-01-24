#include "nconstant.h"
#include "nspawn_entity.h"

namespace scc::ast {
    nspawn_entity::nspawn_entity() = default;
    nspawn_entity::~nspawn_entity() = default;

    void nspawn_entity::to_json(json &j) {
        j["entity"] = entity_name;
        std::vector<json> args;
        for (const auto &arg : arguments) {
            json x;
            if (holds_alternative<nfunction_call_named_argument_ref>(arg)) {
                get<nfunction_call_named_argument_ref>(arg)->to_json(x);
            } else {
                get<expression_ref>(arg)->to_json(x);
            }

            args.push_back(x);
        }

        j["arguments"] = args;
    }
}