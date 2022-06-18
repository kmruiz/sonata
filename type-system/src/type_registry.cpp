#include "type_registry.h"

#include <algorithm>

typedef std::map<std::string, std::shared_ptr<scc::type_system::type>>::value_type map_registry_entry;

namespace scc::type_system {
    type_registry::type_registry() = default;
    type_registry::~type_registry() = default;

    std::shared_ptr<type> type_registry::resolve(const std::string &name) {
        if (map.contains(name)) {
            return map[name];
        }

        auto res = std::make_shared<type>();
        map[name] = res;
        res->name = name;
        res->kind = scc::type_system::type_kind::UNKNOWN;

        return res;
    }

    bool type_registry::defined(const std::string &name) {
        if (map.contains(name)) {
            return map[name]->kind != scc::type_system::type_kind::UNKNOWN;
        }

        return false;
    }

    std::list<std::shared_ptr<type>> type_registry::all_types() {
        // [](const MyMap::value_type& val){return val.second;
        std::list<std::shared_ptr<type>> return_value;
        std::transform(map.begin(), map.end(), std::back_inserter(return_value), [](const map_registry_entry &entry) {
            return entry.second;
        });
        return return_value;
    }
}

