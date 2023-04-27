#pragma once

#include <cstdint>
#include <string_view>

namespace vm::core {
    struct address {
        uint64_t id;
    };

    address make_address();
}
