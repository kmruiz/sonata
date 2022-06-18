#pragma once

#include "../pass.h"
#include "type_registry.h"
#include "memory/internal_modeler.h"

namespace scc::passes::analyzers {
    using namespace scc::type_system;

    class pass_internal_modeler : public pass {
    public:
        explicit pass_internal_modeler(const std::shared_ptr<memory::internal_modeler> &modeler);
        ~pass_internal_modeler() override;

        void execute(scc::ast::ast_root &root) const override;
        diagnostic::diagnostic_phase_id pass_phase() const override;

    private:
        const std::shared_ptr<memory::internal_modeler> modeler;
    };

}
