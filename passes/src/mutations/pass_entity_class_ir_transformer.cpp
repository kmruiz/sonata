#include "pass_entity_class_ir_transformer.h"

namespace scc::passes::mutations {
    using namespace scc::ast;
    using namespace scc::ast::ir;
    using namespace scc::type_system::memory;

    static shared_ptr<field> select_field_from_type(std::shared_ptr<type> &type, std::list<string>::iterator begin, std::list<string>::iterator end);

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

                            nstrf->name = nklass->name + "_" + nfn->name;

                            nstrf->body = std::make_shared<block>();

                            parse_self_refs(ntype, nfn->body.value(), nstrf->body);
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

    void pass_entity_class_ir_transformer::parse_self_refs(std::shared_ptr<type> &type, expression_ref &expr, ast_block &block) const {
        if (std::dynamic_pointer_cast<nclass_self_set>(expr)) {
            auto selfset = std::dynamic_pointer_cast<nclass_self_set>(expr);
            shared_ptr<field> fieldef = select_field_from_type(type, selfset->selector.begin(), selfset->selector.end());

            switch (fieldef->selector.type) {
                case type_system::memory::selector_type::BIT_BAG: {
                    auto bbset = std::make_shared<nstruct_bitbag_set>();
                    bbset->value = selfset->value;
                    bbset->bit = fieldef->selector.offset;

                    block->children.emplace_back(bbset);
                } break;
                case type_system::memory::selector_type::DIRECT: {
                    auto ddset = std::make_shared<nstruct_direct_set>();
                    ddset->value = selfset->value;
                    ddset->field = fieldef->name;

                    block->children.emplace_back(ddset);
                } break;
            }
        } else {
            block->children.emplace_back(expr);
        }
    }

    static shared_ptr<field> select_field_from_type(std::shared_ptr<type> &type, std::list<string>::iterator begin, std::list<string>::iterator end) {
        auto cur = *begin;

        for (auto &f : type->fields) {
            if (f->name == cur) {
                if (++begin == end) {
                    return f;
                }

                return select_field_from_type(f->base_type, begin, end);
            }
        }

        return nullptr;
    }
}

