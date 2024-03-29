#include <thread>

#include "concurrency/spin_lock.h"
#include "mailbox/fiber.h"
#include "api.h"

namespace vmc_ = vm::concurrency;

vma::actor_system *CURRENT_ACTOR_SYSTEM = nullptr;
vmm::mailbox** CURRENT_MAILBOXES = nullptr;
vmm::fiber** CURRENT_FIBERS = nullptr;
thread_count_t CURRENT_FIBERS_COUNT = 0;
thread_count_t CURRENT_MAILBOXES_COUNT = 0;
thread_count_t CURRENT_MAILBOX = 0;
vmc_::spin_lock MAILBOX_LIST_LOCK;

vmc::address mkaddress() {
    return vmc::make_address();
}

vma::actor *mkactor(vmc::address addr, vmc::address supervisor, vma::base_actor_state *state, vmm::mailbox *mb, vma::actor_type *type) {
    return new vma::actor(addr, supervisor, std::unique_ptr<vma::base_actor_state>(state), std::shared_ptr<vmm::mailbox>(mb), std::shared_ptr<vma::actor_type>(type), std::shared_ptr<vma::actor_system>(CURRENT_ACTOR_SYSTEM));
}

void dlactor(vma::actor *actor) {
    delete actor;
}

void actor_receive(vma::actor *rcv, vmm::message *msg) {
    rcv->push_from(std::unique_ptr<vmm::message>(msg));
}

void actor_send(vma::actor *actor, vmm::message *msg, vmc::address &rcv) {
    actor->send(std::unique_ptr<vmm::message>(msg), rcv);
}

void *actor_state(vma::actor *actor) {
    auto state = actor->state_as();
    return static_cast<void *>(state.get());
}

vma::actor_system *mkactorsystem(thread_count_t count) {
    auto actor_system = new vma::actor_system();

    if (count == 0) {
        count = std::thread::hardware_concurrency();
        if (count == 0) {
            count = 1;
        } else {
            count *= 2;
        }
    }

    CURRENT_FIBERS_COUNT = count;
    CURRENT_MAILBOXES_COUNT = count;
    CURRENT_FIBERS = new vmm::fiber*[count];
    CURRENT_MAILBOXES = new vmm::mailbox*[count];
    CURRENT_MAILBOX = 0;

    auto using_system = [actor_system](vm::actor::address addr) -> std::shared_ptr<vm::actor::actor> {
        return actor_system->resolve_by_address(addr);
    };

    for (auto i = 0; i < count; i++) {
        CURRENT_MAILBOXES[i] = new vmm::mailbox();
        CURRENT_FIBERS[i] = new vmm::fiber(std::shared_ptr<vmm::mailbox>(CURRENT_MAILBOXES[i]), using_system);
    }

    CURRENT_ACTOR_SYSTEM = actor_system;
    return CURRENT_ACTOR_SYSTEM;
}

vma::actor_system *getactorsystem() {
    return CURRENT_ACTOR_SYSTEM;
}

vmm::mailbox *getmailbox() {
    auto _lock = MAILBOX_LIST_LOCK.lock();
    auto next_mailbox = (CURRENT_MAILBOX + 1) % CURRENT_MAILBOXES_COUNT;
    auto mb = CURRENT_MAILBOXES[CURRENT_MAILBOX];
    CURRENT_MAILBOX = next_mailbox;
    return mb;
}

void dlactorsystem() {
    for (auto i = 0; i < CURRENT_FIBERS_COUNT; i++) {
        CURRENT_FIBERS[i]->stop();
        delete CURRENT_FIBERS[i];
    }

    delete CURRENT_FIBERS;
    delete CURRENT_ACTOR_SYSTEM;
}

vma::actor *actorsystem_resolve_by_address(const vmc::address &addr) {
    return CURRENT_ACTOR_SYSTEM->resolve_by_address(addr).get();
}

void actorsystem_register_actor(vma::actor *actor) {
    CURRENT_ACTOR_SYSTEM->register_actor(std::shared_ptr<vma::actor>(actor));
}

vma::actor_type *mkactortype(const char *name) {
    return new vma::actor_type(std::string(name));
}

void actortype_register(vma::actor_type *type, const char *msg, const vma::dispatch_message &dispatch) {
    type->dispatch_table[std::string(msg)] = dispatch;
}

vmm::message *mkmsg(vma::actor *sender, const char *name) {
    auto msg = new vmm::message();
    msg->sender = sender->get_address();
    msg->message = std::string(name);

    return msg;
}

void message_set_metadata(vmm::message *msg, const char *name, const char *value) {
    msg->metadata[std::string(name)] = std::string(value);
}

void message_push_arg(vmm::message *msg, const char *name) {
    msg->arguments.emplace_back(name);
}

void *message_pop_arg(vmm::message *msg) {
    auto arg = msg->arguments.back();
    msg->arguments.pop_back();

    return (void *) arg.c_str();
}

void message_send(vma::actor *sender, vmm::message *msg, vma::address &address) {
    sender->send(std::unique_ptr<vmm::message>(msg), address);
}
