#include "pass_entity_class_method_resolution.h"

namespace scc::passes::mutations {
    using namespace scc::ast;

    pass_entity_class_method_resolution::pass_entity_class_method_resolution(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_entity_class_method_resolution::~pass_entity_class_method_resolution() = default;

    void pass_entity_class_method_resolution::execute(ast::ast_root &root) const {
        for (auto &child : root->children) {
            if (std::dynamic_pointer_cast<nclass>(child)) {
            }
        }
    }

    diagnostic::diagnostic_phase_id pass_entity_class_method_resolution::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_ENTITY_METHOD_RESOLUTION;
    }
}

