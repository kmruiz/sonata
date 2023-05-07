//
// Created by kevin on 4/27/23.
//

#include "mailbox.h"

using namespace vm::mailbox;

mailbox::mailbox() {
    this->released = false;
}

mailbox::~mailbox() {

}

void mailbox::enqueue(std::unique_ptr<message> message) {
    if (released) {
        return;
    }

    auto ref = lock.lock();
    queue.push_back(std::move(message));
}

std::unique_ptr<message> mailbox::dequeue() {
    auto ref = lock.lock();
    if (!replies.empty()) {
        auto reply = std::move(replies.front());
        replies.pop_front();

        return reply;
    }

    if (queue.empty()) {
        return {nullptr};
    }

    auto value = std::move(queue.front());
    queue.pop_front();
    return value;
}

void mailbox::wait_for_reply(std::unique_ptr<message> message) {
    auto ref = lock.lock();
    replies.push_back(std::move(message));
}

void mailbox::reply(correlation_id id, const std::string &answer) {
    auto ref = lock.lock();
    for (auto &i : replies) {
        if (i->reply_on == id) {
            i->reply = answer;
            return;
        }
    }
}
