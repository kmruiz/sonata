#pragma once

#include <map>

#include "../core/address.h"
#include "../mailbox/mailbox.h"
#include "../concurrency/spin_lock.h"

namespace vm::actor {
    using vm::mailbox::message;
    using vm::mailbox::mailbox;
    using vm::core::address;

    struct actor_type;
    struct base_actor_state {};

    enum actor_message_process_result {
        OK,
        FAILED,
    };

    class actor {
    public:
        actor(
                address addr,
                address supervisor,
                std::unique_ptr<base_actor_state> initial_state,
                std::shared_ptr<mailbox> mailbox,
                std::shared_ptr<actor_type> type
        );
        ~actor();

        void push_from(std::unique_ptr<message> msg);
        void send(std::unique_ptr<message> msg, address receiver);
        virtual actor_message_process_result process_message(std::unique_ptr<message> message);

        template<class State>
        std::shared_ptr<State> state_as();

    protected:
        std::shared_ptr<base_actor_state> self_state;
        address self_address;
        address self_supervisor;
        std::shared_ptr<actor_type> self_type;
        std::shared_ptr<mailbox> bound_mailbox;
        concurrency::spin_lock polling_lock;
        concurrency::spin_lock processing_lock;
    };
}
