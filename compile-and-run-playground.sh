#!/bin/bash

set -e

PATH_CLANG_LIB_BEGIN=/usr/lib/clang/13/lib/linux/clang_rt.crtbegin-x86_64.o
PATH_CLANG_LIB_END=/usr/lib/clang/13/lib/linux/clang_rt.crtend-x86_64.o

echo -1 | sudo tee /proc/sys/kernel/perf_event_paranoid > /dev/null
cmake --build cmake-build-debug --target scc -j 12
cmake --build cmake-build-debug --target sonata-rt -j 12
./cmake-build-debug/scc -D./cmake-build-debug/debug-diagnostic.json -LDEBUG sn_examples/playground
clang -c sn_examples/playground/extern/printbb.c -o lib.o
clang++ -no-pie output.o lib.o ./cmake-build-debug/runtime/vm/libsonata-rt.a
echo '---------------------------------------------------------------------------------------'
perf record ./a.out
echo
echo '---------------------------------------------------------------------------------------'
rm a.out
rm output.o
rm lib.o