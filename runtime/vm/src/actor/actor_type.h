#pragma once

#include <map>
#include <memory>
#include <functional>

#include "../mailbox/mailbox.h"
#include "actor.h"

namespace vm::actor {
    using vm::mailbox::message;
    enum actor_message_process_result;
    class actor;

    typedef std::function<actor_message_process_result(actor *, std::unique_ptr<message>)> dispatch_message;

    struct actor_type {
        dispatch_message resolve(std::string message_name);

        std::string class_name;
        std::map<std::string, dispatch_message> dispatch_table;
    };
}