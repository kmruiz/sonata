#include <gtest/gtest.h>

#include <utility>

#include "../actor/actor.cpp"
#include "../actor/actor.h"
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
               std::shared_ptr<vm::actor::actor_type> type
           ) : actor(addr, supervisor, std::move(initial_state), std::move(mailbox), std::move(type)) {
    }

    vm::actor::actor_message_process_result process_message(std::unique_ptr<vm::mailbox::message> message) override {
        auto ref = this->processing_lock.lock();
        auto state = this->state_as<actor_state>();
        state->did_run = true;
        return vm::actor::OK;
    }
};

TEST(fiber, processes_a_message_for_an_actor) {
    auto mb = std::make_shared<vm::mailbox::mailbox>();
    auto mock = std::make_shared<fake_actor>(
            vm::core::make_address(),
            vm::core::make_address(),
            std::make_unique<vm::actor::base_actor_state>(actor_state { .did_run = false }),
            mb,
            nullptr
    );

    auto always_mock = [mock](vm::actor::address addr) -> std::shared_ptr<vm::actor::actor> {
        return mock;
    };

    auto fb = std::make_shared<vm::mailbox::fiber>(mb, always_mock);

    auto enqueued_message = std::make_unique<vm::mailbox::message>();
    mock->push_from(std::move(enqueued_message));

    std::this_thread::sleep_for(1ms);
    fb->stop();

    auto state = mock->state_as<actor_state>();
    ASSERT_EQ(state->did_run, true);
}