cmake_minimum_required(VERSION 3.22)
project(type-system)

set(CMAKE_CXX_STANDARD 20)

include_directories(../common/src)
include_directories(../diagnostic/src)

add_library(type-system src/type.h src/type_registry.cpp src/type_registry.h src/memory/internal_modeler.cpp src/memory/internal_modeler.h)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(type-system_tests src/type-system.spec.cpp src/type_registry.test.cpp src/memory/internal_modeler.test.cpp)
target_link_libraries(type-system_tests type-system GTest::GTest GTest::Main diagnostic)
gtest_discover_tests(type-system_tests)