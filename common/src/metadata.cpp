#include "metadata.h"

namespace scc::common {

    void to_json(json &j, const info_comment &comment) {
        j["type"] = "comment";
        j["content"] = comment.content;
    }

    void to_json(json &j, const info_identifier &identifier) {
        j["type"] = "identifier";
        j["content"] = identifier.content;
    }

    void to_json(json &j, const info_integer &integer) {
        j["type"] = "integer";
        j["representation"] = integer.representation;
        j["base"] = integer.base;
    }

    void to_json(json &j, const info_floating &floating) {
        j["type"] = "floating";
        j["representation"] = floating.representation;
        j["base"] = floating.base;
    }

    void to_json(json &j, const info_boolean &boolean) {
        j["type"] = "boolean";
        j["value"] = boolean.value;
    }

    void to_json(json &j, const info_string &string) {
        j["type"] = "string";
        j["value"] = string.content;
    }
}