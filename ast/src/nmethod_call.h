#pragma once

#include <variant>
#include <string>
#include <list>

#include "metadata.h"
#include "nfunction_call.h"
#include "node.h"

namespace scc::ast {
    using std::list;
    using std::variant;
    using std::shared_ptr;

    using namespace scc::common;

    struct nmethod_call : public expression {
        nmethod_call();
        ~nmethod_call();

        expression_ref left;
        std::string method;
        list<variant<nfunction_call_named_argument_ref, expression_ref>> arguments;

        void to_json(json &j) override;
    };
}