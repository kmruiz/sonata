#include "pass_entity_class_ir_transformer.h"

namespace scc::passes::mutations {
    using namespace scc::ast;
    using namespace scc::ast::ir;
    using namespace scc::type_system::memory;

    pass_entity_class_ir_transformer::pass_entity_class_ir_transformer(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_entity_class_ir_transformer::~pass_entity_class_ir_transformer() = default;

    void pass_entity_class_ir_transformer::execute(ast::ast_root &root) const {
        list<node_ref> new_children;

        for (auto &child : root->children) {
            if (std::dynamic_pointer_cast<nclass>(child)) {
                auto nklass = std::dynamic_pointer_cast<nclass>(child);
                if (nklass->type != ast::nclass_type::ENTITY) {
                    continue;
                }

                auto ntype = types->resolve(nklass->name);

                // ----- create struct -----
                auto nstr = std::make_shared<nstruct>();
                nstr->name = nklass->name;

                // define struct fields
                for (auto &s : ntype->layout.storages) {
                    if (std::holds_alternative<bit_bag>(s)) {
                        auto bb = std::get<bit_bag>(s);
                        nstr->fields.emplace_back(nstruct_field { .field_type = ast::ir::BIT_BAG, .name = "bit_bag", .size = bb.size });
                    }
                }

                new_children.emplace_back(nstr);
                // ----- spawn function -----
                auto spawn_fn = std::make_shared<nstruct_function_def>();
                spawn_fn->name = "_spawn";
                spawn_fn->retval = OBJECT;
                spawn_fn->body = std::make_shared<block>();

                new_children.emplace_back(spawn_fn);
                // ----- free function -----
                auto free_fn = std::make_shared<nstruct_function_def>();
                free_fn->name = "_free";
                free_fn->retval = VOID;
                free_fn->body = std::make_shared<block>();

                new_children.emplace_back(free_fn);
                // ----- transform methods -----
                if (nklass->body.has_value()) {
                    for (auto &m : nklass->body.value()->children) {
                        if (std::dynamic_pointer_cast<nlet_function>(m)) {
                            auto nfn = std::dynamic_pointer_cast<nlet_function>(m);
                            auto nstrf = std::make_shared<nstruct_function_def>();

                            nstrf->body = std::make_shared<block>();
                            nstrf->body->children.emplace_back(nfn->body.value());

                            new_children.emplace_back(nstrf);
                        } else {
                            new_children.emplace_back(m);
                        }
                    }
                }
            } else {
                new_children.emplace_back(child);
            }
        }

        root->children = new_children;
    }

    diagnostic::diagnostic_phase_id pass_entity_class_ir_transformer::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_ENTITY_CLASS_IR_TRANSFORMER;
    }
}

