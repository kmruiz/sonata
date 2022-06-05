#include "nconstant.h"
#include "nfunction_call.h"


namespace scc::ast {
    nfunction_call::nfunction_call() = default;
    nfunction_call::~nfunction_call() = default;

    void nfunction_call_named_argument::to_json(json &j) {
        j["name"] = name;
        expression->to_json(j["expression"]);
    }

    void nfunction_call::to_json(json &j) {
        left->to_json(j["left"]);
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