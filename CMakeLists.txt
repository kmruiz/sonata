cmake_minimum_required(VERSION 3.22)
project(scc)

set(CMAKE_CXX_STANDARD 20)

add_subdirectory(discovery)
add_subdirectory(diagnostic)
add_subdirectory(lexer)
add_subdirectory(ast)
add_subdirectory(parser)
add_subdirectory(common)
add_subdirectory(backend-llvm)

include_directories(discovery/src)
include_directories(diagnostic/src)
include_directories(ast/src)
include_directories(lexer/src)
include_directories(common/src)
include_directories(backend-llvm/src)

add_executable(scc main.cpp)
target_link_libraries(scc lexer diagnostic ast parser discovery common backend-llvm)
