#include "type_registry.h"

namespace scc::type_system {
    type_registry::type_registry() = default;
    type_registry::~type_registry() = default;

    std::shared_ptr<type> type_registry::resolve(const std::string &name) {
        if (map.contains(name)) {
            return map[name];
        }

        auto res = std::make_shared<type>();
        map[name] = res;
        res->kind = scc::type_system::type_kind::UNKNOWN;

        return res;
    }

    bool type_registry::defined(const std::string &name) {
        if (map.contains(name)) {
            return map[name]->kind != scc::type_system::type_kind::UNKNOWN;
        }

        return false;
    }
}

