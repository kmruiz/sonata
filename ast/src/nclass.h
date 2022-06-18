#pragma once

#include <variant>
#include <string>

#include "metadata.h"
#include "node.h"

namespace scc::ast {
    using std::variant;
    using std::shared_ptr;
    using namespace scc::common;

    enum class nclass_type : unsigned char {
        VALUE, ENTITY, ENTITY_INTERFACE, CAPABILITY
    };

    enum class nclass_visibility : unsigned char {
        PUBLIC, PRIVATE
    };

    struct nclass_primary_field {
        type_constraints type;
        string name;
        optional<expression_ref> initial_value;
    };

    struct nclass_capability_requirement {
        string field;
        string capability;
    };

    struct nclass : public node {
        nclass_type type;
        nclass_visibility visibility;
        string package;
        string name;
        list<shared_ptr<nclass_primary_field>> fields;
        list<shared_ptr<nclass_capability_requirement>> requirements;
        optional<ast_block> body;

        void to_json(json& j) override;
    };

    struct nclass_self_set : public expression {
        list<string> selector;
        expression_ref value;

        void to_json(json& j) override;
    };

    struct nclass_self_get : public expression {
        list<string> selector;

        void to_json(json& j) override;
    };
}