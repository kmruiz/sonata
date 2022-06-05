cmake_minimum_required(VERSION 3.22)
project(lexer)

set(CMAKE_CXX_STANDARD 20)

include_directories(../diagnostic/src)
include_directories(../common/src)

add_library(lexer src/token_type.h src/token.h src/lexer.cpp src/lexer.h src/token_type.cpp)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(lexer_tests src/lexer.spec.cpp src/lexer.test.cpp)
target_link_libraries(lexer_tests lexer GTest::GTest GTest::Main diagnostic)
gtest_discover_tests(lexer_tests)