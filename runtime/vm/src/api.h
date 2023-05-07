#pragma once

#include "./mailbox/mailbox.h"
#include "./mailbox/fiber.h"
#include "./core/address.h"
#include "./actor/actor.h"
#include "./actor/actor_type.h"
#include "./actor/actor_system.h"

namespace vma = vm::actor;
namespace vmm = vm::mailbox;
namespace vmc = vm::core;

/**
 * Sonata Runtime API
 *
 * This is an stable API that can be used within the compiler when generating code.
 * This API can be linked both statically and dynamically with the application.
 *
 * Do not use this API directly: it's designed only for the compiler.
 * If you want to integrate with the Sonata runtime at this level, use the C++ APIs.
 */

extern "C" {
    typedef uint8_t thread_count_t;

    // Actor API
    vmc::address mkaddress();

    vma::actor *mkactor(vmc::address addr, vmc::address supervisor, vma::base_actor_state *state, vmm::mailbox *mb, vma::actor_type *type, vma::actor_system *system);
    void dlactor(vma::actor *actor);
    void actor_receive(vma::actor *rcv, vmm::message *msg);
    void actor_send(vma::actor *actor, vmm::message *msg, vmc::address &rcv);
    void *actor_state(vma::actor *actor); // actor state is only "typed" at the C++ level.

    // Actor System API
    vma::actor_system *mkactorsystem(thread_count_t count);
    vma::actor_system *getactorsystem();
    void dlactorsystem(vma::actor_system *system);
    vma::actor *actorsystem_resolve_by_address(vma::actor_system *system, const vmc::address &addr);
    void actorsystem_register_actor(vma::actor_system *system, vma::actor *actor);

    // Actor Type API
    vma::actor_type *mkactortype(const char *name);
    void actortype_register(vma::actor_type *type, const char *msg, const vma::dispatch_message &dispatch);
}