#include <gtest/gtest.h>

#include <utility>

#include "../actor/actor.h"
#include "../actor/actor_system.h"
#include "fiber.h"

using namespace std::chrono_literals;

struct actor_state : public vm::actor::base_actor_state {
    bool did_run;
};

class fake_actor : public vm::actor::actor {
public:
    fake_actor(
            vm::core::address addr, vm::core::address supervisor,
               std::unique_ptr<vm::actor::base_actor_state> initial_state,
               std::shared_ptr<vm::mailbox::mailbox> mailbox,
               std::shared_ptr<vm::actor::actor_type> type,
               std::shared_ptr<vm::actor::actor_system> system
           ) : actor(addr, supervisor, std::move(initial_state), std::move(mailbox), std::move(type), std::move(system)) {
    }

    vm::actor::actor_message_process_result process_message(std::unique_ptr<vm::mailbox::message> message) override {
        auto ref = this->processing_lock.lock();
        auto state = std::reinterpret_pointer_cast<actor_state>(this->state_as());
        state->did_run = true;
        return vm::actor::OK;
    }
};

TEST(fiber, processes_a_message_for_an_actor) {
    auto system = std::make_shared<vm::actor::actor_system>();
    auto mb = std::make_shared<vm::mailbox::mailbox>();
    auto mock = std::make_shared<fake_actor>(
            vm::core::make_address(),
            vm::core::make_address(),
            std::make_unique<vm::actor::base_actor_state>(actor_state { .did_run = false }),
            mb,
            nullptr,
            system
    );

    auto always_mock = [mock](vm::actor::address addr) -> std::shared_ptr<vm::actor::actor> {
        return mock;
    };

    auto fb = std::make_shared<vm::mailbox::fiber>(mb, always_mock);

    auto enqueued_message = std::make_unique<vm::mailbox::message>();
    mock->push_from(std::move(enqueued_message));

    std::this_thread::sleep_for(100us);
    fb->stop();

    auto state = std::reinterpret_pointer_cast<actor_state>(mock->state_as());
    ASSERT_EQ(state->did_run, true);
}