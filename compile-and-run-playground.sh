#!/bin/bash

set -e

echo -1 | sudo tee /proc/sys/kernel/perf_event_paranoid
cmake --build cmake-build-debug --target scc -j 12
./cmake-build-debug/scc -D./cmake-build-debug/debug-diagnostic.json -LDEBUG sn_examples/playground
clang output.o
echo '---------------------------------------------------------------------------------------'
perf record ./a.out
echo
echo '---------------------------------------------------------------------------------------'
rm a.out
rm output.o