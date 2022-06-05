#include "nlet.h"

namespace scc::ast {
    nlet::nlet() : mutable_p(false) {};
    nlet::~nlet() = default;

    void nlet::to_json(json &j) {
        j["id"] = this->id;
        j["node"] = "let";
        j["mutable"] = this->mutable_p;
        j["constraints"] = this->constraints;

        if (this->expression.has_value()) {
            json inner;
            this->expression.value()->to_json(inner);
            j["expression"] = inner;
        } else {
            j["expression"] = nullptr;
        }

    }

    nlet_function::nlet_function() = default;
    nlet_function::~nlet_function() = default;

    void nlet_function::to_json(json &j) {
        j["id"] = this->id;
        j["node"] = "let function";
        j["return_type"] = this->return_type;
        std::vector<json> params;

        for (const auto &param : this->parameters) {
            json pj;

            if (std::holds_alternative<nlet_function_named_parameter>(param)) {
                const auto &v = std::get<nlet_function_named_parameter>(param);

                pj["kind"] = "named";
                pj["name"] = v.name;
                pj["type"] = v.type;
            } else {
                const auto &v = std::get<expression_ref>(param);
                pj["kind"] = "expression";
                v->to_json(pj);
            }

            params.emplace_back(pj);
        }
        j["parameters"] = params;

        if (this->body.has_value()) {
            json inner;
            this->body.value()->to_json(inner);
            j["body"] = inner;
        } else {
            j["body"] = nullptr;
        }
    }
}