#include "pass_entity_class_method_resolution.h"

namespace scc::passes::mutations {
    using namespace scc::ast;
    using namespace scc::ast::ir;

    pass_entity_class_method_resolution::pass_entity_class_method_resolution(const std::shared_ptr<type_registry> &types)
        : types(types) {

    }

    pass_entity_class_method_resolution::~pass_entity_class_method_resolution() = default;

    static node_ref recursive_iterate(node_ref node) {
        if (std::dynamic_pointer_cast<nspawn_entity>(node)) {
            auto mcall = std::dynamic_pointer_cast<nspawn_entity>(node);
            auto nfcall = std::make_shared<nfunction_call>();

            auto fcallid = std::make_shared<nidentifier>();
            fcallid->name = mcall->entity_name + "_spawn";

            nfcall->left = fcallid;
            nfcall->arguments.insert(nfcall->arguments.begin(), mcall->arguments.cbegin(), mcall->arguments.cend());

            return nfcall;
        } else if (std::dynamic_pointer_cast<nmethod_call>(node)) {
            auto mcall = std::dynamic_pointer_cast<nmethod_call>(node);
            auto nfcall = std::make_shared<nfunction_call>();

            auto fcallid = std::make_shared<nidentifier>();
            fcallid->name = "EntityTest_" + mcall->method;

            nfcall->left = fcallid;
            nfcall->arguments.insert(nfcall->arguments.begin(), mcall->arguments.cbegin(), mcall->arguments.cend());

            nfcall->arguments.push_front(mcall->left);
            return nfcall;
        } else if (std::dynamic_pointer_cast<nlet>(node)) {
            auto mlet = std::dynamic_pointer_cast<nlet>(node);
            if (mlet->expression.has_value()) {
                auto old_expr = mlet->expression.value();
                auto new_expr = recursive_iterate(old_expr);
                mlet->expression = std::dynamic_pointer_cast<expression>(new_expr);
            }
        }

        return node;
    }

    void pass_entity_class_method_resolution::execute(ast::ast_root &root) const {
        list<node_ref> new_children;

        for (auto &child : root->children) {
            new_children.push_back(recursive_iterate(child));
        }

        root->children = new_children;
    }

    diagnostic::diagnostic_phase_id pass_entity_class_method_resolution::pass_phase() const {
        return diagnostic::diagnostic_phase_id::PASS_ENTITY_METHOD_RESOLUTION;
    }
}

