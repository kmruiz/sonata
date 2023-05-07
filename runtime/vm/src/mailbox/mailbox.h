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

    typedef uint64_t correlation_id;

    struct message {
        core::address sender;
        core::address receiver;
        std::string message;
        std::map<std::string, std::string> metadata;
        std::list<std::string> arguments;
        std::list<message_trace> traces;
        correlation_id reply_on;
        std::string reply;

        inline bool has_reply() {
            return !reply.empty();
        }

        inline bool is_a_reply() {
            return reply_on != 0;
        }
    };

    class mailbox {
    public:
        mailbox();
        ~mailbox();

        void enqueue(std::unique_ptr<message> message);
        std::unique_ptr<message> dequeue();
        void wait_for_reply(std::unique_ptr<message> message);
        void reply(correlation_id id, const std::string &answer);
    private:
        concurrency::spin_lock lock;
        std::list<std::unique_ptr<message>> queue;
        std::list<std::unique_ptr<message>> replies;
        bool released;
    };

}