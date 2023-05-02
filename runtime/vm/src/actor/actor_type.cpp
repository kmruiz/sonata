#include "actor_type.h"

using namespace vm::actor;

dispatch_message actor_type::resolve(std::string message_name) {
    return dispatch_table[message_name];
}
