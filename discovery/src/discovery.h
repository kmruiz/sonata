#pragma once

#include <list>
#include <string>

namespace scc::discovery {
    using std::string;
    using std::list;

    class discovery {
    public:
        explicit discovery();
        ~discovery();

        list<string> discover_directories(const list<string> &directories);
    };
}