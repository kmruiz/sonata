#pragma once

#include <atomic>
#include <memory>
#include <chrono>

namespace vm::concurrency {
    typedef std::shared_ptr<std::atomic_bool> spin_lock_latch;

    struct spin_lock_ref {
        explicit spin_lock_ref(spin_lock_latch ref);
        ~spin_lock_ref();

        void unlock();
    private:
        spin_lock_latch ref;
    };

    class spin_lock {
    public:
        spin_lock();
        ~spin_lock();

        spin_lock_ref lock();
    private:
        spin_lock_latch latch;
    };

}
