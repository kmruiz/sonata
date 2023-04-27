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

TEST(mailbox, acts_as_fifo) {
    auto mb = vm::mailbox::mailbox();
    auto emsg1 = std::make_unique<vm::mailbox::message>();
    auto emsg2 = std::make_unique<vm::mailbox::message>();
    emsg1->message = "1";
    emsg2->message = "2";

    mb.enqueue(std::move(emsg1));
    mb.enqueue(std::move(emsg2));

    auto dmsg1 = mb.dequeue();
    auto dmsg2 = mb.dequeue();

    ASSERT_EQ(dmsg1->message, "1");
    ASSERT_EQ(dmsg2->message, "2");
    ASSERT_FALSE(mb.dequeue());
}