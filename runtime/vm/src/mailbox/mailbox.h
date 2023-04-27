#pragma once

#include <string>
#include <map>
#include <list>

#include "../core/address.h"
#include "../concurrency/spin_lock.h"

namespace vm::mailbox {
    struct message_trace {
        core::address stepped_actor;
        std::string message;
    };

    struct message {
        core::address sender;
        core::address receiver;
        std::string message;
        std::map<std::string, std::string> metadata;
        std::list<std::string> arguments;
        std::list<message_trace> traces;
    };

    class mailbox {
    public:
        mailbox();
        ~mailbox();

        void enqueue(std::unique_ptr<message> message);
        std::unique_ptr<message> dequeue();
        void release();
    private:
        concurrency::spin_lock lock;
        std::list<std::unique_ptr<message>> queue;
        bool released;
    };

}