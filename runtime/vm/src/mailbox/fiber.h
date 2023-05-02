#pragma once

#include <optional>
#include <thread>
#include <atomic>
#include <functional>

#include "../actor/actor.h"

namespace vm::mailbox {
    using vm::actor::actor;
    using vm::actor::base_actor_state;
    using vm::actor::address;

    typedef std::function<std::shared_ptr<actor>(address)> actor_resolver;

    class fiber {
    public:
        fiber(
                std::shared_ptr<mailbox> mailbox,
                actor_resolver resolver
        );
        ~fiber();

        void run();
        void stop();
    private:
        std::atomic_bool running;
        std::thread running_thread;
        std::shared_ptr<mailbox> bound_mailbox;
        actor_resolver current_resolver;
    };
}