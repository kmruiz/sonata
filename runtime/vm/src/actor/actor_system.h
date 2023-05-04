#pragma once

#include <memory>
#include <map>

#include "../concurrency/spin_lock.h"
#include "actor.h"

namespace vm::actor {
    class actor_system {
    public:
        actor_system();
        ~actor_system();

        std::shared_ptr<actor> resolve_by_address(const address &addr);
        void register_actor(const std::shared_ptr<actor>& actor_to_register);
    private:
        std::map<address, std::shared_ptr<actor>> registry;
        concurrency::spin_lock lock;
    };
}
