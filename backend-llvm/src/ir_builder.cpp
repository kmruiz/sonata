#include "ir_builder.h"
#include "diagnostic.h"

namespace scc::backend::llvm {
    ir_builder::ir_builder(
            std::shared_ptr<LLVMContext> &context,
            std::shared_ptr<IRBuilder<>> &ir,
            std::shared_ptr<Module> &module,
            std::shared_ptr<FunctionPassManager> &pass_manager)
            : _context(context), _builder(ir), _module(module), _pass_manager(pass_manager)
            {

    }

    void ir_builder::build_ir(const scc::ast::ast_root &document) {
        D_START_PHASE(scc::diagnostic::diagnostic_phase_id::GENERATE_LLVM_IR);
        std::vector<Type *> NoArgs(0);
        FunctionType *FT = FunctionType::get(Type::getInt32Ty(*_context), NoArgs, false);

        Function *F = Function::Create(FT, Function::ExternalLinkage, "main", _module.get());
        BasicBlock *BB = BasicBlock::Create(*_context, "entry", F);
        _builder->SetInsertPoint(BB);

        for (const auto &node : document->children) {

        }
//        for (const auto &node_uq: document->children) {
//            auto node = node_uq.get();
//            to_value(node);
//        }

        _builder->CreateRet(ConstantInt::get(*_context, APInt(32, 0)));
        verifyFunction(*F);
        D_END_PHASE();
        D_START_PHASE(scc::diagnostic::diagnostic_phase_id::OPTIMIZE_LLVM_IR);
        _pass_manager->run(*F);
        D_END_PHASE();
    }
}