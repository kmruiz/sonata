#include "discovery.h"
#include "diagnostic.h"

#include <filesystem>
#include <iterator>

namespace scc::discovery {

    discovery::discovery() {

    }

    discovery::~discovery() {

    }

    list<string> discovery::discover_source_files(const list<string> &directories) {
        D_START_PHASE(diagnostic::diagnostic_phase_id::DISCOVERY);
        list<string> unique = list(directories);

        unique.sort();
        unique.unique();

        std::ostringstream oss;
        copy(unique.begin(), unique.end(), std::ostream_iterator<string>(oss, ","));

        D_DEBUG("Discovering files in directories. ", { diagnostic::diagnostic_log_marker { .key = "directories", .value = oss.str() }});

        list<string> paths;

        for (const auto &directory : unique) {
            for (const auto& dirEntry : std::filesystem::recursive_directory_iterator(directory)) {
                if (dirEntry.path().extension() == ".sn") {
                    D_DEBUG("Found file. It's going to be processed because it has extension .sn", { diagnostic::diagnostic_log_marker { .key = "file", .value = dirEntry.path() }});
                    paths.push_back(dirEntry.path());
                } else {
                    D_DEBUG("Found file. It's not going to be processed because it doesn't have extension .sn", { diagnostic::diagnostic_log_marker { .key = "file", .value = dirEntry.path() }});
                }
            }
        }

        paths.sort();
        paths.unique();

        D_END_PHASE();
        return paths;
    }

}