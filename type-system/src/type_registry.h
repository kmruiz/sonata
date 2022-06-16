#pragma once

#include "type.h"

#include <map>
#include <string>

namespace scc::type_system {
    class type_registry {
    public:
        explicit type_registry();
        ~type_registry();

        std::shared_ptr<type> resolve(const std::string &name);
        bool defined(const std::string &name);
    private:
        std::map<std::string, std::shared_ptr<type>> map;
    };

}