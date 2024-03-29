cmake_minimum_required(VERSION 3.22)
project(runtime_vm)

set(CMAKE_CXX_STANDARD 20)

add_library(sonata-rt
        src/mailbox/mailbox.cpp
        src/mailbox/mailbox.h
        src/core/address.cpp
        src/core/address.h
        src/concurrency/spin_lock.cpp
        src/concurrency/spin_lock.h
        src/actor/actor.cpp
        src/actor/actor.h
        src/actor/actor_type.cpp
        src/actor/actor_type.h
        src/actor/actor_system.cpp
        src/actor/actor_system.h
        src/mailbox/fiber.cpp
        src/mailbox/fiber.h
        src/api.cpp
        src/api.h)

target_compile_options(sonata-rt PRIVATE -fpie)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(sonata-rt_tests src/core/address.test.cpp src/mailbox/mailbox.test.cpp src/mailbox/fiber.test.cpp src/actor/actor.test.cpp)
target_link_libraries(sonata-rt_tests sonata-rt GTest::GTest GTest::Main)
gtest_discover_tests(sonata-rt_tests)