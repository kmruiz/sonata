#include "pass_entity_class_method_resolution.h"

namespace scc::passes::mutations {
    using namespace scc::ast;
    using namespace scc::ast::ir;

    pass_entity_class_method_resolution::pass_entity_class_method_resolution(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_entity_class_method_resolution::~pass_entity_class_method_resolution() = default;

    void pass_entity_class_method_resolution::execute(ast::ast_root &root) const {
        list<node_ref> new_children;

        for (auto &child : root->children) {
            if (std::dynamic_pointer_cast<nmethod_call>(child)) {
                auto mcall = std::dynamic_pointer_cast<nmethod_call>(child);
                auto nfcall = std::make_shared<nfunction_call>();

                auto fcallid = std::make_shared<nidentifier>();
                fcallid->name = "EntityTest_" + mcall->method;

                nfcall->left = fcallid;
                nfcall->arguments.insert(nfcall->arguments.begin(), mcall->arguments.cbegin(), mcall->arguments.cend());

                nfcall->arguments.push_front(mcall->left);
                new_children.push_back(nfcall);
            } else {
                new_children.push_back(child);
            }
        }

        root->children = new_children;
    }

    diagnostic::diagnostic_phase_id pass_entity_class_method_resolution::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_ENTITY_METHOD_RESOLUTION;
    }
}

