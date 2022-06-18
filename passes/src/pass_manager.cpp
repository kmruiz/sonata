#include "pass_manager.h"

#include "analyzers/pass_detect_classes.h"
#include "diagnostic.h"

namespace scc::passes {

    pass_manager::pass_manager(std::shared_ptr<scc::type_system::type_registry> &types) :
        validations({
            std::make_unique<analyzers::pass_detect_classes>(types)
        }), mutations({

        }) {

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