#include "pass_manager.h"

#include "diagnostic.h"

namespace scc::passes {

    pass_manager::pass_manager(const std::initializer_list<pass> &validation_passes, const std::initializer_list<pass> &mutation_passes) :
        validations(validation_passes), mutations(mutation_passes) {

    }

    void pass_manager::run(ast::ast_root &root) {
        for (auto &v : validations) { // these can run in parallel at some point
            D_START_PHASE(v.pass_phase());
            v.execute(root);
            D_END_PHASE();
        }

        for (auto &v : mutations) {
            D_START_PHASE(v.pass_phase());
            v.execute(root);
            D_END_PHASE();
        }
    }
}