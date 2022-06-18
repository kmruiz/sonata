#pragma once

#include "../pass.h"
#include "type_registry.h"

namespace scc::passes::analyzers {
    using namespace scc::type_system;

    class pass_detect_classes : public pass {
    public:
        explicit pass_detect_classes(const std::shared_ptr<type_registry> &types);
        ~pass_detect_classes() override;

        void execute(scc::ast::ast_root &root) const override;
        diagnostic::diagnostic_phase_id pass_phase() const override;

    private:
        std::shared_ptr<type_registry> types;
    };

}
