cmake_minimum_required(VERSION 3.22)
project(diagnostic)

set(CMAKE_CXX_STANDARD 20)

find_package(nlohmann_json 3.2.0 REQUIRED)

add_library(diagnostic src/diagnostic.cpp src/diagnostic.h)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(diagnostic_tests src/diagnostic.cpp)
target_link_libraries(diagnostic_tests GTest::GTest GTest::Main)
gtest_discover_tests(diagnostic_tests)