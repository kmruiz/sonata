#include <random>
#include "address.h"

using namespace vm::core;

static std::random_device rd;
static std::mt19937_64 gen(rd());

address vm::core::make_address() {
    return { .id = gen() };
}