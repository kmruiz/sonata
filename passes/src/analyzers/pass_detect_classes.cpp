#include "pass_detect_classes.h"

namespace scc::passes::analyzers {

    pass_detect_classes::pass_detect_classes(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_detect_classes::~pass_detect_classes() = default;

    void pass_detect_classes::execute(ast::ast_root &root) const {

    }

    diagnostic::diagnostic_phase_id pass_detect_classes::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_DETECT_CLASSES;
    }
}