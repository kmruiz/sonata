#include "actor.h"

#include <utility>

#include "actor_system.h"
#include "actor_type.h"

namespace vm::actor {
    actor::actor(address addr,
                 address supervisor,
                 std::unique_ptr<base_actor_state> initial_state,
                 std::shared_ptr<mailbox> mailbox,
                 std::shared_ptr<actor_type> type,
                 std::shared_ptr<actor_system> system
    ) :
            self_address(addr),
            self_supervisor(supervisor),
            self_state(std::move(initial_state)),
            bound_mailbox(std::move(mailbox)),
            self_type(std::move(type)),
            system(std::move(system)) {
    }

    actor::~actor() = default;

    void actor::push_from(std::unique_ptr<message> msg) {
        bound_mailbox->enqueue(std::move(msg));
    }

    void actor::send(std::unique_ptr<message> msg, address receiver) {
        auto receiver_actor = system->resolve_by_address(receiver);
        receiver_actor->push_from(std::move(msg));
    }

    actor_message_process_result actor::process_message(std::unique_ptr<message> message) {
        auto name = message->message;
        auto call = self_type->resolve(name);

        return call(this, std::move(message));
    }

    template<class State>
    std::shared_ptr<State> actor::state_as() {
        return std::static_pointer_cast<State>(this->self_state);
    }
}