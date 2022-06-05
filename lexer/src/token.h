#pragma once

#include <string>
#include <memory>
#include <variant>

#include "token_type.h"
#include "metadata.h"

namespace scc::lexer {
    using std::string;
    using std::unique_ptr;
    using std::variant;
    using namespace scc::common;

    typedef variant<info_comment, info_identifier, info_integer, info_floating, info_string> token_metadata;

    struct token_source {
        string source;
        size_t line, column;
    };

    struct token {
        token_type type;
        token_source source;
        token_metadata metadata;
    };
}
