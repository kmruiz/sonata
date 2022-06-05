//
// Created by kevin on 5/21/22.
//

#include "discovery.h"
#include "diagnostic.h"

#include <filesystem>

namespace scc::discovery {

    discovery::discovery() {

    }

    discovery::~discovery() {

    }

    list<string> discovery::discover_directories(const list<string> &directories) {
        D_START_PHASE(diagnostic::diagnostic_phase_id::DISCOVERY);
        list<string> paths;

        for (const auto &directory : directories) {
            for (const auto& dirEntry : std::filesystem::recursive_directory_iterator(directory)) {
                if (dirEntry.path().extension() == ".sn") {
                    D_DEBUG("Found file. It's going to be processed because it has extension .sn", { diagnostic::diagnostic_log_marker { .key = "file", .value = dirEntry.path() }});
                    paths.push_back(dirEntry.path());
                } else {
                    D_DEBUG("Found file. It's not going to be processed because it doesn't have extension .sn", { diagnostic::diagnostic_log_marker { .key = "file", .value = dirEntry.path() }});
                }
            }
        }

        D_END_PHASE();
        return paths;
    }

}