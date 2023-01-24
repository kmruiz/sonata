#pragma once

#include <variant>
#include <string>
#include <list>

#include "metadata.h"
#include "node.h"
#include "nfunction_call.h"

namespace scc::ast {
    using std::list;
    using std::variant;
    using std::shared_ptr;

    using namespace scc::common;

    struct nspawn_entity : public expression {
        nspawn_entity();
        ~nspawn_entity();

        std::string entity_name;
        list<variant<nfunction_call_named_argument_ref, expression_ref>> arguments;

        void to_json(json &j) override;
    };
}