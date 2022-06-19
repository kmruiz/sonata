#pragma once

#include "../node.h"

namespace scc::ast::ir {
    enum nstruct_field_type {
        BIT_BAG, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING, OBJECT, VOID
    };

    struct nstruct_field {
        nstruct_field_type field_type;
        std::string name;
        unsigned int size; // for when applies
    };

    struct nstruct : public node {
        std::string name;
        std::list<nstruct_field> fields;

        void to_json(json &j) override;
    };

    struct nstruct_malloc : public expression {
        std::string type;

        void to_json(json &j) override;
    };

    struct nstruct_free : public expression {
        expression_ref ref;

        void to_json(json &j) override;
    };

    struct nstruct_register_to_mailbox : public expression {
        expression_ref self;

        void to_json(json &j) override;
    };

    struct nstruct_function_def : public expression {
        std::string name;
        std::list<nstruct_field_type> signature;
        nstruct_field_type retval;
        ast_block body;

        void to_json(json &j) override;
    };

    struct nstruct_function_call : public expression {
        std::list<expression_ref> arguments;

        void to_json(json &j) override;
    };

    struct nstruct_bitbag_set : public expression {
        unsigned int bit;
        expression_ref value;

        void to_json(json &j) override;
    };

    struct nstruct_direct_set : public expression {
        std::string field;
        expression_ref value;

        void to_json(json &j) override;
    };

    struct nstruct_bitbag_get : public expression {
        unsigned int bit;
        void to_json(json &j) override;
    };

    struct nstruct_direct_get : public expression {
        std::string field;
        void to_json(json &j) override;
    };
}