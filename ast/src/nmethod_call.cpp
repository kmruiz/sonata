#include "nconstant.h"
#include "nmethod_call.h"

namespace scc::ast {
    nmethod_call::nmethod_call() = default;
    nmethod_call::~nmethod_call() = default;

    void nmethod_call::to_json(json &j) {
        left->to_json(j["left"]);
        j["method"] = method;
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