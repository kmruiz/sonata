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

        list<string> discover_source_files(const list<string> &directories);
    };
}