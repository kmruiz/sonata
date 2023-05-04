#include "actor_system.h"

namespace vm::actor {
    actor_system::actor_system() {

    }

    actor_system::~actor_system() {

    }

    std::shared_ptr<actor> actor_system::resolve_by_address(const address &addr) {
        auto locked = lock.lock();
        auto it = registry.find(addr);

        if (it == registry.end()) {
            return {};
        }

        return it->second;
    }

    void actor_system::register_actor(const std::shared_ptr<actor> &actor_to_register) {
        auto locked = lock.lock();
        registry[actor_to_register->self_address] = actor_to_register;
    }
}