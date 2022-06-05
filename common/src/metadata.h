#pragma once

#include <nlohmann/json.hpp>
#include <string>

namespace scc::common {
    using std::string;
    using json = nlohmann::json;

    enum class numeric_base : unsigned char {
        BINARY = 2,
        OCTAL = 8,
        DECIMAL = 10,
        HEXADECIMAL = 16
    };

    struct info_comment {
        string content;
    };

    struct info_identifier {
        string content;
    };

    struct info_integer {
        string representation;
        numeric_base base;
    };

    struct info_floating {
        string representation;
        numeric_base base;
    };

    struct info_boolean {
        bool value;
    };

    struct info_string {
        string content;
    };

    void to_json(json &j, const info_comment &comment);
    void to_json(json &j, const info_identifier &identifier);
    void to_json(json &j, const info_integer &integer);
    void to_json(json &j, const info_floating &floating);
    void to_json(json &j, const info_boolean &boolean);
    void to_json(json &j, const info_string &string);
}
