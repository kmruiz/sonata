#include <vector>

#include "node.h"

namespace scc::ast {
    using std::vector;

    void node_qualified_context::to_json(json &j) {
        j["name"] = this->name;
    }

    void root::to_json(json &j) {
        j["id"] = this->id;
        j["type"] = "root";

        vector<json> childrenj;
        for (const auto &node : this->children) {
            json cj;
            node->to_json(cj);
            childrenj.emplace_back(cj);
        }

        j["children"] = childrenj;
        this->context.to_json(j["context"]);
    }

    void block::to_json(json &j) {
        j["id"] = this->id;
        j["type"] = "block";

        vector<json> childrenj;
        for (const auto &node : this->children) {
            json cj;
            node->to_json(cj);
            childrenj.emplace_back(cj);
        }

        j["children"] = childrenj;
    }

    void to_json(json &j, const node_id &id) {
        j["id"] = id.id;
        j["source"] = id.source;
        j["row"] = id.row;
        j["column"] = id.column;
    }

    void to_json(json& j, const type_constraint_none& tcnone) {
        j["type_constraint"] = "none";
    }

    void to_json(json& j, const type_constraint_sum& tcsum) {
        j["type_constraint"] = "sum";
        std::vector<json> sum;
        for (auto const &type : tcsum.types) {
            sum.emplace_back(type);
        }
        j["sum"] = sum;
    }

    void to_json(json& j, const type_constraint_equality& tceq) {
        j["type_constraint"] = "equals";
        j["equals"] = tceq.type;
    }

    void to_json(json& j, const type_constraint_constant& tconstant) {
        j["type_constraint"] = "constant";
        j["base"] = tconstant.base_constraint;
        j["value"] = tconstant.value;
    }

    void to_json(json& j, const type_constraint_generic& tcgen) {
        j["type_constraint"] = "generic";
        std::vector<json> params;
        for (auto const &type : tcgen.parameters) {
            params.emplace_back(type);
        }
        j["parameters"] = params;
    }

    void to_json(json& j, const type_constraints& tconstraints) {
        if (std::holds_alternative<type_constraint_none>(tconstraints)) {
            j = std::get<type_constraint_none>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_equality>(tconstraints)) {
            j = std::get<type_constraint_equality>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_generic>(tconstraints)) {
            j = std::get<type_constraint_generic>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_sum>(tconstraints)) {
            j = std::get<type_constraint_sum>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_constant>(tconstraints)) {
            j = std::get<type_constraint_constant>(tconstraints);
        }
    }
}