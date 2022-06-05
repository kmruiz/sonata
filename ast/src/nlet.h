#pragma once

#include "node.h"

namespace scc::ast {
    struct nlet : public node {
        nlet();
        ~nlet();

        bool mutable_p;
        string name;
        type_constraints constraints;
        optional<expression_ref> expression;

        void to_json(json& j) override;
    };

    struct nlet_function_named_parameter {
        string name;
        type_constraints type;
    };

    typedef variant<nlet_function_named_parameter, expression_ref> nlet_function_parameter;

    struct nlet_function : public node {
        nlet_function();
        ~nlet_function();

        string name;
        list<nlet_function_parameter> parameters;
        type_constraints return_type;
        optional<expression_ref> body;

        void to_json(json& j) override;
    };
}