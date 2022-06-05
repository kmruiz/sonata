#include "nconstant.h"

namespace scc::ast {
    nconstant::nconstant() = default;
    nconstant::~nconstant() = default;

    void nconstant::to_json(json &j) {
        j["id"] = this->id;
        j["node"] = "constant";

        if (holds_alternative<info_integer>(this->content)) {
            j["value"] = get<info_integer>(this->content);
        }

        if (holds_alternative<info_floating>(this->content)) {
            j["value"] = get<info_floating>(this->content);
        }

        if (holds_alternative<info_boolean>(this->content)) {
            j["value"] = get<info_boolean>(this->content);
        }

        if (holds_alternative<info_string>(this->content)) {
            j["value"] = get<info_string>(this->content);
        }
    }
}