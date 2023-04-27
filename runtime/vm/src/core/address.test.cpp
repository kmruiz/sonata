#include <gtest/gtest.h>

#include "address.h"

TEST(address, are_unique) {
    auto addr1 = vm::core::make_address();
    auto addr2 = vm::core::make_address();
    ASSERT_FALSE(addr1.id == addr2.id);
}