#include "pass_value_class_ir_transformer.h"

namespace scc::passes::mutations {

    pass_value_class_ir_transformer::pass_value_class_ir_transformer(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_value_class_ir_transformer::~pass_value_class_ir_transformer() = default;

    void pass_value_class_ir_transformer::execute(ast::ast_root &root) const {

    }

    diagnostic::diagnostic_phase_id pass_value_class_ir_transformer::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_VALUE_CLASS_IR_TRANSFORMER;
    }
}

