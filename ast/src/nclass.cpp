#include "nconstant.h"
#include "nclass.h"


namespace scc::ast {
    nconstant::nconstant() = default;
    nconstant::~nconstant() = default;

    void nconstant::to_json(json &j) {
        j["id"] = this->id;
        j["node"] = "constant";

        if (holds_alternative<info_integer>(this->content)) {
            j["value"] = get<info_integer>(this->content);
        }

        if (holds_alternative<info_floating>(this->content)) {
            j["value"] = get<info_floating>(this->content);
        }

        if (holds_alternative<info_boolean>(this->content)) {
            j["value"] = get<info_boolean>(this->content);
        }

        if (holds_alternative<info_string>(this->content)) {
            j["value"] = get<info_string>(this->content);
        }
    }

    void ast::nclass::to_json(json &j) {
        switch (type) {
            case nclass_type::CAPABILITY:
                j["type"] = "capability";
                break;
            case nclass_type::ENTITY:
                j["type"] = "entity";
                break;
            case nclass_type::VALUE:
                j["type"] = "value";
                break;
        }

        switch (visibility) {
            case nclass_visibility::PUBLIC:
                j["visibility"] = "public";
                break;
            case nclass_visibility::PRIVATE:
                j["visibility"] = "private";
                break;
        }

        j["package"] = package;
        j["name"] = name;

        std::vector<json> fields_json;
        for (const auto &f : fields) {
            json fj;
            fj["type"] = f->type;
            fj["name"] = f->name;
            if (f->initial_value.has_value()) {
                f->initial_value.value()->to_json(fj["initial_value"]);
            }
            fields_json.emplace_back(fj);
        }

        j["fields"] = fields_json;
        std::vector<json> requirements_json;
        for (const auto &r : requirements) {
            json rj;
            rj["field"] = r->field;
            rj["capability"] = r->capability;
            requirements_json.emplace_back(rj);
        }
        j["requirements"] = requirements_json;
        std::vector<json> allowance_json;

        if (body.has_value()) {
            body.value()->to_json(j["body"]);
        }
    }

    void nclass_self_set::to_json(json &j) {
        std::vector<string> js;
        json jv;

        for (auto &k : selector) {
            js.emplace_back(k);
        }

        value->to_json(jv);

        j["selector"] = js;
        j["value"] = jv;
    }

    void nclass_self_get::to_json(json &j) {
        std::vector<string> js;

        for (auto &k : selector) {
            js.emplace_back(k);
        }

        j["selector"] = js;
    }
}