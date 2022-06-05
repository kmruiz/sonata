cmake_minimum_required(VERSION 3.22)
project(common)

set(CMAKE_CXX_STANDARD 20)

add_library(common src/metadata.cpp src/metadata.h)
