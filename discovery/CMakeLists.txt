cmake_minimum_required(VERSION 3.22)
project(discovery)

set(CMAKE_CXX_STANDARD 20)

include_directories(../diagnostic/src)
add_library(discovery src/discovery.cpp src/discovery.h)

enable_testing()
find_package(GTest REQUIRED)
include(GoogleTest)