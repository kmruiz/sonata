#include "ir_builder.h"
#include "diagnostic.h"
#include "ast.h"

namespace scc::backend::llvm {
    ir_builder::ir_builder(
            shared_ptr<LLVMContext> &context,
            shared_ptr<IRBuilder<>> &ir,
            shared_ptr<Module> &module,
            shared_ptr<FunctionPassManager> &pass_manager)
            : _context(context), _builder(ir), _module(module), _pass_manager(pass_manager) {

    }

    void ir_builder::build_ir(const scc::ast::ast_root &document) {
        D_START_PHASE(scc::diagnostic::diagnostic_phase_id::GENERATE_LLVM_IR);
        std::vector<Type *> NoArgs(0);
        FunctionType *FT = FunctionType::get(Type::getInt32Ty(*_context), NoArgs, false);

        Function *F = Function::Create(FT, Function::ExternalLinkage, "main", _module.get());
        BasicBlock *BB = BasicBlock::Create(*_context, "entry", F);
        _builder->SetInsertPoint(BB);

        for (const auto &node: document->children) {
            if (std::dynamic_pointer_cast<ast::nfunction_call>(node) != nullptr) {
                auto function_call = std::dynamic_pointer_cast<ast::nfunction_call>(node);
                auto cte = std::dynamic_pointer_cast<ast::nidentifier>(function_call->left);
                auto fn = _module->getFunction(cte->name);
                std::vector<Value *> args;
                for (auto &arg: function_call->arguments) {
                    auto expr = std::get<ast::expression_ref>(arg);
                    auto str = std::dynamic_pointer_cast<ast::nconstant>(expr);
                    args.emplace_back(_builder->CreateGlobalStringPtr(get<common::info_string>(str->content).content));
                }
                _builder->CreateCall(fn, args);
            }

            if (std::dynamic_pointer_cast<ast::nlet_function>(node) != nullptr) {
                auto nletfn = std::dynamic_pointer_cast<ast::nlet_function>(node);
                if(nletfn->external) {
                    register_extern_function(nletfn);
                } else {
                    register_function(nletfn);
                }
            }
        }

        _builder->CreateRet(ConstantInt::get(*_context, APInt(32, 0)));
        verifyFunction(*F);
        D_END_PHASE();
        D_START_PHASE(scc::diagnostic::diagnostic_phase_id::OPTIMIZE_LLVM_IR);
        _pass_manager->run(*F);
        D_END_PHASE();
    }

#define DD(type, fn) if (std::dynamic_pointer_cast<type>(expr) != nullptr) { return fn(std::dynamic_pointer_cast<type>(expr)); }

    Value *ir_builder::to_value(const expression_ref &expr) {
        DD(ast::nfunction_call, to_value)

        return nullptr;
    }

#undef DD

    Value *ir_builder::to_value(const shared_ptr<ast::nfunction_call> &expr) {
        auto function_call = std::dynamic_pointer_cast<ast::nfunction_call>(expr);
        auto cte = std::dynamic_pointer_cast<ast::nidentifier>(function_call->left);
        auto fn = _module->getFunction(cte->name);
        std::vector<Value *> args;
        for (auto &arg: function_call->arguments) {
            auto argexpr = std::get<ast::expression_ref>(arg);
            auto str = std::dynamic_pointer_cast<ast::nconstant>(argexpr);
            args.emplace_back(_builder->CreateGlobalStringPtr(get<common::info_string>(str->content).content));
        }
        return _builder->CreateCall(fn, args);
    }

    void ir_builder::register_extern_function(const shared_ptr<ast::nlet_function> &nletfn) {
        std::vector<Type *> parameters;
        for (auto p : nletfn->parameters) {
            parameters.emplace_back(PointerType::getUnqual(Type::getInt32Ty(*_context)));
        }

        _module->getOrInsertFunction(nletfn->name,
                                    FunctionType::get(Type::getInt32Ty(*_context),
                                                      parameters, true));
    }

    void ir_builder::register_function(const shared_ptr<ast::nlet_function> &nletfn) {
        FunctionType *llvmfnty = FunctionType::get(Type::getVoidTy(*_context), false);
        auto llvmfn = Function::Create(llvmfnty, llvm::GlobalValue::PrivateLinkage, nletfn->name, *_module);

        auto new_builder = std::make_shared<IRBuilder<>>(*_context);
        auto old_builder = _builder;

        auto block = BasicBlock::Create(*_context, "entry", llvmfn);
        _builder = new_builder;
        _builder->SetInsertPoint(block);
        auto value = to_value(nletfn->body.value());
        _builder->CreateRet(value);

        _builder = old_builder;
        _params.clear();
        verifyFunction(*llvmfn);
        _pass_manager->run(*llvmfn);
    }
}