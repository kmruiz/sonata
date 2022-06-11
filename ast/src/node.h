#pragma once

#include <nlohmann/json.hpp>
#include <iostream>
#include <list>
#include <variant>
#include <optional>
#include <memory>

namespace scc::ast {
    using std::string;
    using std::variant;
    using std::list;
    using json = nlohmann::json;
    using std::optional;

    struct node_id {
        uint32_t id;
        string source;
        uint32_t row, column;
    };

    struct type_constraint_none {};
    struct type_constraint_sum;
    struct type_constraint_equality { string type; };
    struct type_constraint_constant {
        type_constraint_equality base_constraint;
        string value;
    };
    struct type_constraint_generic;
    struct type_constraint_sum;

    typedef variant<type_constraint_none, type_constraint_equality, type_constraint_generic, type_constraint_sum, type_constraint_constant> type_constraints;

    struct type_constraint_generic {
        string base;
        list<type_constraints> parameters;
    };
    struct type_constraint_sum {
        list<type_constraints> types;
    };

    struct node {
        node_id id;

        virtual void to_json(json& j) = 0;
    };

    struct expression : public node {
        type_constraints constraints;
    };

    typedef std::shared_ptr<node> node_ref;
    typedef std::shared_ptr<expression> expression_ref;

    struct root : node {
        list<node_ref> children;

        void to_json(json &j) override;
    };

    struct block : node {
        list<node_ref> children;

        void to_json(json &j) override;
    };

    typedef std::shared_ptr<root> ast_root;
    typedef std::shared_ptr<block> ast_block;

    void to_json(json& j, const node_id& id);
    void to_json(json& j, const type_constraint_none& tcnone);
    void to_json(json& j, const type_constraint_sum& tcsum);
    void to_json(json& j, const type_constraint_equality& tceq);
    void to_json(json& j, const type_constraint_constant& tconstant);
    void to_json(json& j, const type_constraint_generic& tcgen);
    void to_json(json& j, const type_constraints & tconstraints);
}