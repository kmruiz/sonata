cmake_minimum_required(VERSION 3.22)
project(ast)

set(CMAKE_CXX_STANDARD 20)

include_directories(../diagnostic/src)
include_directories(../common/src)

add_library(ast
        src/node.cpp src/node.h
        src/nclass.cpp src/nclass.h
        src/nconstant.cpp src/nconstant.h
        src/nfunction_call.cpp src/nfunction_call.h
        src/nidentifier.cpp src/nidentifier.h
        src/nlet.cpp src/nlet.h
        src/ast.h
        )

target_link_libraries(ast common)