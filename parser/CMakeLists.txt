cmake_minimum_required(VERSION 3.22)
project(parser)

set(CMAKE_CXX_STANDARD 20)

include_directories(../diagnostic/src)
include_directories(../ast/src)
include_directories(../common/src)
include_directories(../lexer/src)

add_library(parser src/parser.cpp src/parser.h)
target_link_libraries(parser lexer ast)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(parser_tests
        src/parser.spec.cpp
        src/parser_class.test.cpp
        src/parser_function_call.test.cpp
        src/parser_let.test.cpp
        src/parser_type.test.cpp
        src/test-includes/includes.hpp
)
target_link_libraries(parser_tests lexer GTest::GTest GTest::Main diagnostic parser)
gtest_discover_tests(parser_tests)