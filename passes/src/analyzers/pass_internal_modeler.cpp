#include "pass_internal_modeler.h"

namespace scc::passes::analyzers {

    pass_internal_modeler::pass_internal_modeler(const std::shared_ptr<memory::internal_modeler> &modeler)
        : modeler(modeler) {

    }

    pass_internal_modeler::~pass_internal_modeler() = default;

    void pass_internal_modeler::execute(ast::ast_root &root) const {
        modeler.
    }

    diagnostic::diagnostic_phase_id pass_internal_modeler::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_INTERNAL_MODELER;
    }
}