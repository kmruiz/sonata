#!/bin/bash

set -e

cmake --build cmake-build-debug --target scc -j 12
./cmake-build-debug/scc -D ./cmake-build-debug/debug-diagnostic.json sn_examples/playground
clang output.o
echo '---------------------------------------------------------------------------------------'
./a.out
echo
echo '---------------------------------------------------------------------------------------'
rm a.out
rm output.o