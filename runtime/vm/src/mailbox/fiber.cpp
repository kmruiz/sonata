#include "fiber.h"

#include <utility>

namespace vm::mailbox {
    fiber::fiber(
            std::shared_ptr<mailbox> mailbox,
            actor_resolver resolver
    ) :
    bound_mailbox(std::move(mailbox)),
    running(true),
    current_resolver(resolver),
    running_thread(std::thread([this]() {
        this->run();
    }))
    {
    }

    fiber::~fiber() {
        this->stop();
    }

    void fiber::run() {
        bool stopped = false;
        while (!running.compare_exchange_strong(stopped, false, std::memory_order_acquire)) {
            stopped = false;

            auto msg = bound_mailbox->dequeue();
            if (msg) {
                auto receiver = current_resolver(msg->receiver);

                if (receiver) {
                    receiver->process_message(std::move(msg));
                }
            }

            std::this_thread::sleep_for(std::chrono::microseconds(50));
        }
    }

    void fiber::stop() {
        running.exchange(false, std::memory_order_acquire);
        if (this->running_thread.joinable()) {
            this->running_thread.join();
        }
    }
}