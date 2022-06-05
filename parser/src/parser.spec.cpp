#include <gtest/gtest.h>
#include "diagnostic.h"

int main(int argc, char **argv) {
    testing::InitGoogleTest(&argc, argv);
    auto result = RUN_ALL_TESTS();
    if (result != 0) {
        scc::diagnostic::dump_diagnostic("parser-diagnostic.json");
    }
    return result;
}