#pragma once

#include <variant>
#include <string>

#include "metadata.h"
#include "node.h"

namespace scc::ast {
    using std::variant;
    using namespace scc::common;

    enum class nconstant_type : unsigned char {
        INTEGER, FLOATING, BOOLEAN, STRING
    };

    typedef variant<info_integer, info_floating, info_boolean, info_string> nconstant_info;

    struct nconstant : public expression {
        nconstant();
        ~nconstant();

        nconstant_type type;
        nconstant_info content;

        void to_json(json& j) override;
    };
}