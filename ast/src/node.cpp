#include <vector>

#include "node.h"

namespace scc::ast {
    using std::vector;

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

    void to_json(json& j, const type_constraint_covariant& tcova) {
        j["type_constraint"] = "covariant";
        j["on"] = tcova.of_type;
    }

    void to_json(json& j, const type_constraint_contravariant& tcntra) {
        j["type_constraint"] = "contravariant";
        j["on"] = tcntra.of_type;
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

    void to_json(json& j, const variant<type_constraint_equality, type_constraint_covariant, type_constraint_contravariant, type_constraint_generic, type_constraint_sum, type_constraint_constant> &tcinner) {
        if (std::holds_alternative<type_constraint_equality>(tcinner)) {
            j = std::get<type_constraint_equality>(tcinner);
        }

        if (std::holds_alternative<type_constraint_covariant>(tcinner)) {
            j = std::get<type_constraint_covariant>(tcinner);
        }

        if (std::holds_alternative<type_constraint_contravariant>(tcinner)) {
            j = std::get<type_constraint_contravariant>(tcinner);
        }

        if (std::holds_alternative<type_constraint_generic>(tcinner)) {
            j = std::get<type_constraint_generic>(tcinner);
        }

        if (std::holds_alternative<type_constraint_sum>(tcinner)) {
            j = std::get<type_constraint_sum>(tcinner);
        }

        if (std::holds_alternative<type_constraint_constant>(tcinner)) {
            j = std::get<type_constraint_constant>(tcinner);
        }
    }

    void to_json(json& j, const type_constraints& tconstraints) {
        if (std::holds_alternative<type_constraint_none>(tconstraints)) {
            j = std::get<type_constraint_none>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_equality>(tconstraints)) {
            j = std::get<type_constraint_equality>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_covariant>(tconstraints)) {
            j = std::get<type_constraint_covariant>(tconstraints);
        }

        if (std::holds_alternative<type_constraint_contravariant>(tconstraints)) {
            j = std::get<type_constraint_contravariant>(tconstraints);
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