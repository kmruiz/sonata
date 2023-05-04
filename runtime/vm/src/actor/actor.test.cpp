#include <gtest/gtest.h>

#include <utility>

#include "actor_type.h"
#include "actor_system.h"
#include "actor.h"

struct actor_state : public vm::actor::base_actor_state {
    bool called;
};

TEST(actor, dispatches_messages) {
    auto system = std::make_shared<vm::actor::actor_system>();
    auto msg = std::make_unique<vm::mailbox::message>();
    msg->message = "hello";

    auto instance_type = std::make_shared<vm::actor::actor_type>();
    instance_type->class_name = "Greeter";
    instance_type->dispatch_table["hello"] = [](vm::actor::actor *i, std::unique_ptr<vm::mailbox::message> msg) -> vm::actor::actor_message_process_result {
        i->state_as<actor_state>()->called = true;
        return vm::actor::OK;
    };

    auto instance = std::make_shared<vm::actor::actor>(
        vm::core::make_address(),
        vm::core::make_address(),
        std::make_unique<vm::actor::base_actor_state>(actor_state { .called = false }),
        std::shared_ptr<vm::actor::mailbox>(),
        instance_type,
        system
    );

    instance->process_message(std::move(msg));
    auto state = instance->state_as<actor_state>();
    ASSERT_EQ(state->called, true);
}

TEST(actor, sends_messages_to_other_actors) {
    auto system = std::make_shared<vm::actor::actor_system>();
    auto mb = std::make_shared<vm::actor::mailbox>();
    auto msg = std::make_unique<vm::mailbox::message>();
    msg->message = "hello";

    auto instance_type = std::make_shared<vm::actor::actor_type>();
    instance_type->class_name = "Greeter";
    instance_type->dispatch_table["hello"] = [](vm::actor::actor *i, std::unique_ptr<vm::mailbox::message> msg) -> vm::actor::actor_message_process_result {
        i->state_as<actor_state>()->called = true;
        return vm::actor::OK;
    };

    const vm::core::address &receiver_address = vm::core::make_address();
    auto instance = std::make_shared<vm::actor::actor>(
            receiver_address,
            vm::core::make_address(),
            std::make_unique<vm::actor::base_actor_state>(actor_state { .called = false }),
            mb,
            instance_type,
            system
    );

    auto sender_instance = std::make_shared<vm::actor::actor>(
            vm::core::make_address(),
            vm::core::make_address(),
            std::make_unique<vm::actor::base_actor_state>(actor_state { .called = false }),
            mb,
            instance_type,
            system
    );

    system->register_actor(instance);
    system->register_actor(sender_instance);

    sender_instance->send(std::move(msg), receiver_address);
    auto msg_in_mb = mb->dequeue();
    instance->process_message(std::move(msg_in_mb));

    auto state = instance->state_as<actor_state>();
    ASSERT_EQ(state->called, true);
}

TEST(actor, that_a_trace_is_added) {
    auto system = std::make_shared<vm::actor::actor_system>();
    auto mb = std::make_shared<vm::actor::mailbox>();
    auto msg = std::make_unique<vm::mailbox::message>();
    msg->message = "hello";

    const vm::core::address &receiver_address = vm::core::make_address();
    const vm::core::address &sender_address = vm::core::make_address();

    auto instance = std::make_shared<vm::actor::actor>(
            receiver_address,
            vm::core::make_address(),
            std::make_unique<vm::actor::base_actor_state>(actor_state { .called = false }),
            mb,
            nullptr,
            system
    );

    auto sender = std::make_shared<vm::actor::actor>(
            sender_address,
            vm::core::make_address(),
            std::make_unique<vm::actor::base_actor_state>(actor_state { .called = false }),
            mb,
            nullptr,
            system
    );

    system->register_actor(instance);
    sender->send(std::move(msg), receiver_address);

    auto msg_in_mb = mb->dequeue();

    ASSERT_EQ(msg_in_mb->traces.size(), 1);

    auto trace = msg_in_mb->traces.front();
    ASSERT_EQ(trace.message, "hello");
    ASSERT_EQ(trace.stepped_actor.id, sender_address.id);
}