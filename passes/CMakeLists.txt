cmake_minimum_required(VERSION 3.22)
project(parser)

set(CMAKE_CXX_STANDARD 20)

include_directories(../diagnostic/src)
include_directories(../ast/src)
include_directories(../common/src)

add_library(passes
        src/pass_manager.cpp src/pass_manager.h
        src/pass.h)

target_link_libraries(passes ast)
#
#enable_testing()
#find_package(GTest REQUIRED)
#include(GoogleTest)
#
#add_executable(passes_test
#        src/parser.spec.cpp
#        src/parser_class.test.cpp
#        src/parser_function_call.test.cpp
#        src/parser_let.test.cpp
#        src/test-includes/includes.hpp
#)
#target_link_libraries(passes_test lexer GTest::GTest GTest::Main diagnostic passes)
#gtest_discover_tests(passes_test)