#pragma once

#include <cstdint>
#include <string_view>

namespace vm::core {
    struct address {
        uint64_t id;
    };

    address make_address();
}

namespace std
{
    template<> struct less<vm::core::address>
    {
        bool operator() (const vm::core::address& lhs, const vm::core::address& rhs) const
        {
            return lhs.id < rhs.id;
        }
    };
}