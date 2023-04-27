#include <gtest/gtest.h>

#include "mailbox.h"

TEST(mailbox, pushes_and_pulls_message_once) {
    auto mb = vm::mailbox::mailbox();
    auto enqueued_message = std::make_unique<vm::mailbox::message>();
    enqueued_message->message = "example";

    mb.enqueue(std::move(enqueued_message));
    auto dequeued_msg = mb.dequeue();

    ASSERT_EQ(dequeued_msg->message, "example");
    ASSERT_FALSE(mb.dequeue());
}