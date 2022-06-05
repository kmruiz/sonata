#pragma once

#include <variant>
#include <string>
#include <list>

#include "metadata.h"
#include "node.h"

namespace scc::ast {
    using std::list;
    using std::variant;
    using std::shared_ptr;

    using namespace scc::common;

    struct nfunction_call_named_argument : public expression {
        string name;
        expression_ref expression;

        void to_json(json &j) override;
    };

    typedef shared_ptr<nfunction_call_named_argument> nfunction_call_named_argument_ref;

    struct nfunction_call : public expression {
        nfunction_call();
        ~nfunction_call();

        expression_ref left;
        list<variant<nfunction_call_named_argument_ref, expression_ref>> arguments;

        void to_json(json &j) override;
    };
}