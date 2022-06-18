#pragma once

#include "type.h"

#include <map>
#include <string>
#include <list>

namespace scc::type_system {
    class type_registry {
    public:
        explicit type_registry();
        ~type_registry();

        std::shared_ptr<type> resolve(const std::string &name);
        bool defined(const std::string &name);
        std::list<std::shared_ptr<type>> all_types();
    private:
        std::map<std::string, std::shared_ptr<type>> map;
    };

}