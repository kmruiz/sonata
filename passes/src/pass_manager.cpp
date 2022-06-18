#include "pass_manager.h"

#include "analyzers/pass_detect_classes.h"
#include "analyzers/pass_internal_modeler.h"

#include "diagnostic.h"

namespace scc::passes {

    pass_manager::pass_manager(std::shared_ptr<scc::type_system::type_registry> &types) {
        validations.emplace_back(std::make_unique<analyzers::pass_detect_classes>(types));
        validations.emplace_back(std::make_unique<analyzers::pass_internal_modeler>(std::make_shared<type_system::memory::internal_modeler>(64, types)));
    }

    void pass_manager::run(ast::ast_root &root) {
        for (auto &v : validations) { // these can run in parallel at some point
            D_START_PHASE(v->pass_phase());
            v->execute(root);
            D_END_PHASE();
        }

        for (auto &v : mutations) {
            D_START_PHASE(v->pass_phase());
            v->execute(root);
            D_END_PHASE();
        }
    }
}