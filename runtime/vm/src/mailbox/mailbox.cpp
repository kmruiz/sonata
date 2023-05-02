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
    if (queue.empty()) {
        return {nullptr};
    }

    auto value = std::move(queue.front());
    queue.pop_front();
    return value;
}
