#include "actor.h"

#include <utility>

namespace vm::actor {
    actor::actor(address addr,
                 address supervisor,
                 std::unique_ptr<base_actor_state> initial_state,
                 std::shared_ptr<mailbox> mailbox,
                 std::shared_ptr<actor_type> type
    ) :
            self_address(addr),
            self_supervisor(supervisor),
            self_state(std::move(initial_state)),
            bound_mailbox(std::move(mailbox)),
            self_type(std::move(type)) {

    }


    actor::~actor() {

    }

    void actor::push_from(std::unique_ptr<message> msg) {
        bound_mailbox->enqueue(std::move(msg));
    }

    void actor::send(std::unique_ptr<message> msg, address receiver) {

    }

    actor_message_process_result actor::process_message(std::unique_ptr<message> message) {
        return FAILED;
    }

    template<class State>
    std::shared_ptr<State> actor::state_as() {
        return std::static_pointer_cast<State>(this->self_state);
    }
}