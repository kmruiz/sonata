#pragma once

#include "type_registry.h"
#include "../pass.h"

namespace scc::passes::mutations {
    using namespace scc::type_system;

    class pass_entity_class_ir_transformer : public pass {
    public:
        explicit pass_entity_class_ir_transformer(const std::shared_ptr<type_registry> &types);
        ~pass_entity_class_ir_transformer() override;

        void execute(scc::ast::ast_root &root) const override;
        diagnostic::diagnostic_phase_id pass_phase() const override;
    private:
        const std::shared_ptr<type_registry> types;
    };
}