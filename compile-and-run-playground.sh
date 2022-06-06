#!/bin/bash

set -e

./cmake-build-debug/scc -D ./cmake-build-debug/debug-diagnostic.json sn_examples/playground
clang output.o
echo '---------------------------------------------------------------------------------------'
./a.out
echo
echo '---------------------------------------------------------------------------------------'
