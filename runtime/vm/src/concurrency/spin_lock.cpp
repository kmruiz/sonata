#include "spin_lock.h"

#include <memory>
#include <utility>

namespace vm::concurrency {
    spin_lock_ref::spin_lock_ref(spin_lock_latch ref) : ref(std::move(ref)) {
    }

    spin_lock_ref::~spin_lock_ref() {
        this->ref->store(false);
    }

    void spin_lock_ref::unlock() {
        this->ref->store(false);
    }

    spin_lock::spin_lock() {
        this->latch = std::make_shared<std::atomic_bool>(false);
    }

    spin_lock::~spin_lock() = default;

    spin_lock_ref spin_lock::lock() {
        bool unlatched = false;
        while (!latch->compare_exchange_strong(unlatched, true, std::memory_order_acquire)) {
            unlatched = false;
        }

        return spin_lock_ref(this->latch);
    }
}