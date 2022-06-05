cmake_minimum_required(VERSION 3.22)
project(backend-llvm)

set(CMAKE_CXX_STANDARD 20)

execute_process(COMMAND llvm-config --libs OUTPUT_VARIABLE LIBS)
execute_process(COMMAND llvm-config --system-libs OUTPUT_VARIABLE SYS_LIBS)
execute_process(COMMAND llvm-config --ldflags OUTPUT_VARIABLE LDF)
execute_process(COMMAND llvm-config --includedir OUTPUT_VARIABLE INCLUDE_DIR)
execute_process(COMMAND llvm-config --cxxflags OUTPUT_VARIABLE CMAKE_CXX_FLAGS)

string(STRIP ${LIBS} LIBS)
string(STRIP ${SYS_LIBS} SYS_LIBS)
string(STRIP ${LDF} LDF)
string(STRIP ${INCLUDE_DIR} INCLUDE_DIR)
string(STRIP ${CMAKE_CXX_FLAGS} CMAKE_CXX_FLAGS)

include_directories(../ast/src)
include_directories(../diagnostic/src)
include_directories(../common/src)

add_definitions(-fexceptions)
add_library(backend-llvm src/backend.cpp src/backend.h src/ir_builder.cpp src/ir_builder.h src/intrinsics/intrinsics.h src/intrinsics/internal_std_io.cpp)
target_link_libraries(backend-llvm ${LIBS} ${SYS_LIBS} ${LDF} diagnostic ast)

target_include_directories(backend-llvm PUBLIC ${INCLUDE_DIR} /usr/lib/llvm-13/include/)
