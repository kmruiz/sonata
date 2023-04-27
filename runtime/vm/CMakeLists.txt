cmake_minimum_required(VERSION 3.22)
project(runtime_vm)

set(CMAKE_CXX_STANDARD 20)


add_library(sonata-rt src/mailbox/mailbox.cpp src/mailbox/mailbox.h src/core/address.cpp src/core/address.h src/concurrency/spin_lock.cpp src/concurrency/spin_lock.h)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(sonata-rt_tests src/core/address.test.cpp src/mailbox/mailbox.test.cpp)
target_link_libraries(sonata-rt_tests sonata-rt GTest::GTest GTest::Main)
gtest_discover_tests(sonata-rt_tests)