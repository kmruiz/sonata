#include "pass_manager.h"

namespace scc::passes {

    pass_manager::pass_manager(const std::initializer_list<pass> &validation_passes, const std::initializer_list<pass> &mutation_passes) :
        validations(validation_passes), mutations(mutation_passes) {

    }

    void pass_manager::run(ast::ast_root &root) {

    }
}