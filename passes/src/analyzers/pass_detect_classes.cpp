#include "pass_detect_classes.h"
#include "ast.h"

static scc::type_system::type_kind map_ast_type(scc::ast::nclass_type type) {
    switch (type) {
        case scc::ast::nclass_type::VALUE:
            return scc::type_system::type_kind::VALUE;
        case scc::ast::nclass_type::ENTITY:
        case scc::ast::nclass_type::ENTITY_INTERFACE:
            return scc::type_system::type_kind::ENTITY;
        case scc::ast::nclass_type::CAPABILITY:
            return scc::type_system::type_kind::CAPABILITY;
    }

    return scc::type_system::type_kind::VALUE;
}

namespace scc::passes::analyzers {

    pass_detect_classes::pass_detect_classes(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_detect_classes::~pass_detect_classes() = default;

    void pass_detect_classes::execute(ast::ast_root &root) const {
        for (auto &child : root->children) {
            if (std::dynamic_pointer_cast<scc::ast::nclass>(child)) {
                auto nclass = std::dynamic_pointer_cast<scc::ast::nclass>(child);
                auto type = types->resolve(nclass->name);

                type->kind = map_ast_type(nclass->type);

                for (auto &f : nclass->fields) {
                    auto base_type = types->resolve(std::get<scc::ast::type_constraint_equality>(f->type).type);
                    auto zero = memory::selector { .type = type_system::memory::selector_type::DIRECT, .offset = 0 };

                    type->fields.emplace_back(std::make_shared<field>(field { .base_type = base_type, .name = f->name, .selector = zero}));
                }
            }
        }
    }

    diagnostic::diagnostic_phase_id pass_detect_classes::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_DETECT_CLASSES;
    }
}