#include "ir_builder.h"
#include "diagnostic.h"
#include "ast.h"

namespace scc::backend::llvm {
    ir_builder::ir_builder(
            shared_ptr<LLVMContext> &context,
            shared_ptr<IRBuilder<>> &ir,
            shared_ptr<Module> &module,
            shared_ptr<FunctionPassManager> &pass_manager,
            shared_ptr<type_registry> &sonata_types)
            : _context(context), _builder(ir), _module(module), _pass_manager(pass_manager),
              _sonata_types(sonata_types) {

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
                to_value(node);
            }

            if (std::dynamic_pointer_cast<ast::nlet_function>(node) != nullptr) {
                auto nletfn = std::dynamic_pointer_cast<ast::nlet_function>(node);
                if (nletfn->external) {
                    register_extern_function(nletfn);
                } else {
                    register_function(nletfn);
                }
            }

            if (std::dynamic_pointer_cast<ast::ir::nstruct_function_def>(node) != nullptr) {
                auto nstrdef = std::dynamic_pointer_cast<ast::ir::nstruct_function_def>(node);
                register_function(nstrdef);
            }

            if (std::dynamic_pointer_cast<ast::nlet>(node) != nullptr) {
                auto nletexpr = std::dynamic_pointer_cast<ast::nlet>(node);
                Value *body = ConstantInt::get(*_context, APInt(32, 0));
                if (nletexpr->expression.has_value()) {
                    body = to_value(nletexpr->expression.value());
                }

                _locals[nletexpr->name] = body;
            }

            if (std::dynamic_pointer_cast<ast::ir::nstruct>(node) != nullptr) {
                auto nstruct = std::dynamic_pointer_cast<ast::ir::nstruct>(node);
                register_struct(nstruct);
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
        DD(ast::nconstant, to_value)
        DD(ast::nidentifier, to_value)
        DD(ast::ir::nstruct_malloc, to_value)
        DD(ast::ir::nstruct_free, to_value)
        DD(ast::ir::nstruct_function_call, to_value)
        DD(ast::ir::nstruct_bitbag_set, to_value)
        DD(ast::ir::nstruct_direct_set, to_value)
        DD(ast::ir::nstruct_bitbag_get, to_value)
        DD(ast::ir::nstruct_direct_get, to_value)

        return nullptr;
    }

#undef DD

    Value *ir_builder::to_value(const node_ref &expr) {
        if (std::dynamic_pointer_cast<expression>(expr)) {
            auto kexpr = std::dynamic_pointer_cast<expression>(expr);
            return to_value(kexpr);
        }

        return nullptr;
    }

    Value *ir_builder::to_value(const shared_ptr<ast::nfunction_call> &expr) {
        auto function_call = std::dynamic_pointer_cast<ast::nfunction_call>(expr);
        auto cte = std::dynamic_pointer_cast<ast::nidentifier>(function_call->left);
        auto fn = _module->getFunction(cte->name);
        std::vector<Value *> args;
        for (auto &arg: function_call->arguments) {
            auto argexpr = std::get<ast::expression_ref>(arg);
            auto value = to_value(argexpr);
            args.emplace_back(value);
        }
        return _builder->CreateCall(fn, args);
    }

    Value *ir_builder::to_value(const shared_ptr<ast::nconstant> &expr) {
        switch (expr->type) {
            case ast::nconstant_type::BOOLEAN: {
                auto value = get<info_boolean>(expr->content).value;
                return ConstantInt::get(*_context, APInt(1, value ? 1 : 0));
            }
            case ast::nconstant_type::INTEGER: {
                auto value = get<info_integer>(expr->content).representation;
                char *end;
                return ConstantInt::get(*_context, APInt(32, std::strtol(value.c_str(), &end,
                                                                         (int) get<info_integer>(expr->content).base)));
            }
            case ast::nconstant_type::FLOATING: {
                auto value = get<info_floating>(expr->content).representation;
                char *end;
                return ConstantFP::get(*_context, APFloat(std::strtof(value.c_str(), &end)));
            }
            case ast::nconstant_type::STRING: {
                auto value = get<info_string>(expr->content).content;
                if (_strings.contains(value)) {
                    return _strings[value];
                } else {
                    auto global_ptr = _builder->CreateGlobalStringPtr(value);
                    _strings[value] = global_ptr;
                    return global_ptr;
                }
            }
        }

        return nullptr;
    }

    Value *ir_builder::to_value(const shared_ptr<ast::nidentifier> &expr) {
        auto varname = expr->name;
        if (_locals.contains(varname)) {
            return _locals[varname];
        }

        return _params[varname];
    }

    void ir_builder::register_extern_function(const shared_ptr<ast::nlet_function> &nletfn) {
        std::vector<Type *> parameters;
        for (auto &p: nletfn->parameters) {
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

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_malloc> &expr) {
        std::vector<Type *> parameters{Type::getInt32Ty(*_context)};
        auto malloc = _module->getOrInsertFunction("malloc",
                                                   FunctionType::get(Type::getInt32Ty(*_context), parameters, false));

        auto type_def = _sonata_types->resolve(expr->type);
        auto mem_layout = type_def->layout;

        return _builder->CreateCall(malloc, {ConstantInt::get(*_context, APInt(32, mem_layout.size_in_bytes))});
    }

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_free> &expr) {
        auto parent = _params["self"];

        std::vector<Type *> parameters{Type::getInt32Ty(*_context)};
        auto free = _module->getOrInsertFunction("free",
                                                 FunctionType::get(Type::getInt32Ty(*_context), parameters, false));

        return _builder->CreateCall(free, { parent });
    }

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_function_call> &expr) {
        return nullptr;
    }

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_bitbag_set> &expr) {
        auto parent = _params["self"];
        auto bitval = to_value(expr->value);

        Value *CondV = _builder->CreateICmpEQ(bitval, ConstantInt::get(*_context, APInt(1, 1)));

        Function *TheFunction = _builder->GetInsertBlock()->getParent();
        BasicBlock *ThenBB = BasicBlock::Create(*_context, "then", TheFunction);
        BasicBlock *ElseBB = BasicBlock::Create(*_context, "else");
        BasicBlock *MergeBB = BasicBlock::Create(*_context, "ifcont");

        _builder->CreateCondBr(CondV, ThenBB, ElseBB);

        _builder->SetInsertPoint(ThenBB);
        Value *ThenCurrentValuePtr = _builder->CreateGEP(parent->getType()->getContainedType(0), parent, ConstantInt::get(*_context, APInt(32, 0)));
        Value *ThenCurrentValue = _builder->CreateLoad(Type::getInt32Ty(*_context), ThenCurrentValuePtr);
        Value *ThenMappedValue = _builder->CreateShl(ConstantInt::get(*_context, APInt(32, 1)), expr->bit);
        Value *ThenValueToSet = _builder->CreateOr(ThenCurrentValue, ThenMappedValue);
        Value *ThenV = _builder->CreateStore(ThenValueToSet, ThenCurrentValuePtr);
        _builder->CreateBr(MergeBB);
        ThenBB = _builder->GetInsertBlock();

        TheFunction->getBasicBlockList().push_back(ElseBB);
        _builder->SetInsertPoint(ElseBB);

        Value *ElseCurrentValue = _builder->CreateGEP(parent->getType()->getContainedType(0), parent,ConstantInt::get(*_context, APInt(32, 0)));
        Value *ElseMappedValue = _builder->CreateNot(_builder->CreateShl(ConstantInt::get(*_context, APInt(32, 1)), expr->bit));
        Value *ElseValueToSet = _builder->CreateAnd(ElseCurrentValue, ElseMappedValue);
        Value *ElseV = _builder->CreateStore(ElseValueToSet, ElseCurrentValue);

        _builder->CreateBr(MergeBB);
        ElseBB = _builder->GetInsertBlock();

        TheFunction->getBasicBlockList().push_back(MergeBB);
        _builder->SetInsertPoint(MergeBB);
        PHINode *PN = _builder->CreatePHI(Type::getVoidTy(*_context), 2, "iftmp");

        PN->addIncoming(ThenV, ThenBB);
        PN->addIncoming(ElseV, ElseBB);

        return parent;
    }

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_direct_set> &expr) {
        auto parent = _params["self"];
        auto directval = to_value(expr->value);

        return _builder->CreateInsertValue(parent, directval, expr->index);
    }

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_bitbag_get> &expr) {
        auto parent = _params["self"];

        Value *CurBitBagPtr = _builder->CreateGEP(parent->getType()->getContainedType(0), parent, ConstantInt::get(*_context, APInt(32, 0)));
        Value *CurBitBag = _builder->CreateLoad(Type::getInt32Ty(*_context), CurBitBagPtr);
        Value *ShiftRight = _builder->CreateLShr(CurBitBag, ConstantInt::get(*_context, APInt(32, expr->bit)));
        Value *GetMaskedValue = _builder->CreateAnd(ShiftRight, ConstantInt::get(*_context, APInt(32, 1)));

        return GetMaskedValue;
    }

    Value *ir_builder::to_value(const shared_ptr<ast::ir::nstruct_direct_get> &expr) {
        auto parent = _params["self"];
        Value *CurDirectPtr = _builder->CreateGEP(parent->getType()->getContainedType(0), parent,ConstantInt::get(*_context, APInt(32, expr->index)));
        Value *CurDirect = _builder->CreateLoad(Type::getInt32Ty(*_context), CurDirectPtr);
        return CurDirect;
    }

    void ir_builder::register_function(const shared_ptr<ast::ir::nstruct_function_def> &letfn) {
        std::vector<Type *> parameters;
        auto selfType = PointerType::get(_types[letfn->struct_name], 0);

        parameters.emplace_back(selfType);
        for (auto &funp: letfn->signature) {
            parameters.emplace_back(Type::getInt32Ty(*_context));
        }

        FunctionType *llvmfnty = FunctionType::get(selfType, parameters, false);
        auto llvmfn = Function::Create(llvmfnty, llvm::GlobalValue::PrivateLinkage, letfn->name, *_module);

        auto args = llvmfn->args();
        auto iter = args.begin();
        auto defargiter = letfn->signature.begin();

        _params["self"] = &(*iter);
        iter++;

        while (iter != args.end()) {
            _params[(*defargiter).name] = &(*iter);
            iter++;
            defargiter++;
        }

        auto new_builder = std::make_shared<IRBuilder<>>(*_context);
        auto old_builder = _builder;

        auto block = BasicBlock::Create(*_context, "entry", llvmfn);
        _builder = new_builder;
        _builder->SetInsertPoint(block);
        auto value = to_value(letfn->body);
        _builder->CreateRet(value);

        _builder = old_builder;
        _params.clear();
        verifyFunction(*llvmfn);
        _pass_manager->run(*llvmfn);
    }

    void ir_builder::register_struct(const shared_ptr<ast::ir::nstruct> &nstruct) {
        std::vector<Type *> StructFields;
        StructType *structType = StructType::create(*_context, nstruct->name);
        _types[nstruct->name] = structType;

        for (auto &field: nstruct->fields) {
//            StructFields.push_back(_types[field.field_type]);
            StructFields.push_back(Type::getInt32Ty(*_context));
        }

        structType->setBody(StructFields);
    }

    Value *ir_builder::to_value(const ast_block &expr) {
        Value *last = nullptr;
        for (auto &c: expr->children) {
            last = to_value(c);
        }

        return last;
    }
}