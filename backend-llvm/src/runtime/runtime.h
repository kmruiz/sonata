#pragma once

#include <memory>
#include <utility>

#include "llvm/IR/LLVMContext.h"
#include "llvm/ADT/APFloat.h"
#include "llvm/ADT/Optional.h"
#include "llvm/ADT/STLExtras.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Constants.h"
#include "llvm/IR/DerivedTypes.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Type.h"
#include "llvm/IR/Verifier.h"
#include "llvm/Support/TargetRegistry.h"
#include "llvm/Support/FileSystem.h"
#include "llvm/Support/Host.h"
#include "llvm/Support/TargetSelect.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Target/TargetMachine.h"
#include "llvm/Target/TargetOptions.h"

namespace scc::backend::llvm::runtime {
    using namespace ::llvm;
    using namespace ::llvm;
    using namespace std;

    class runtime {
    public:
        explicit runtime(shared_ptr<LLVMContext> ctx, shared_ptr<Module> mod): _context(std::move(ctx)), _module(std::move(mod)) {};
        ~runtime() = default;

        inline Value *mkactorsystem(shared_ptr<IRBuilder<>> &builder, Value *value) {
            auto fn = _module->getOrInsertFunction("mkactorsystem", FunctionType::get(Type::getVoidTy(*_context), { Type::getInt8Ty(*_context) }, false));
            return builder->CreateCall(fn, value);
        }

        inline Value *dlactorsystem(shared_ptr<IRBuilder<>> &builder) {
            auto fn = _module->getOrInsertFunction("dlactorsystem", FunctionType::get(Type::getVoidTy(*_context), { Type::getInt8Ty(*_context) }, false));
            return builder->CreateCall(fn);
        }

        inline Value *mkaddress(shared_ptr<IRBuilder<>> &builder) {
            auto fn = _module->getOrInsertFunction("mkaddress", FunctionType::get(Type::getInt64Ty(*_context), {}, false));
            return builder->CreateCall(fn);
        }

        inline Value *getactorsystem(shared_ptr<IRBuilder<>> &builder) {
            auto fn = _module->getOrInsertFunction("getactorsystem", FunctionType::get(Type::getInt32PtrTy(*_context), {}, false));
            return builder->CreateCall(fn);
        }

        inline Value *getmailbox(shared_ptr<IRBuilder<>> &builder) {
            auto fn = _module->getOrInsertFunction("getmailbox", FunctionType::get(Type::getInt32PtrTy(*_context), {}, false));
            return builder->CreateCall(fn);
        }

        inline Value *mkactor(shared_ptr<IRBuilder<>> &builder, Value *address, Value *supervisor, Value *initial, Value *mb, Value *type) {
            auto fn = _module->getOrInsertFunction(
                    "mkactor",
                    FunctionType::get(Type::getInt32PtrTy(*_context), {
                            Type::getInt64Ty(*_context), // actor address
                            Type::getInt64Ty(*_context), // actor supervisor address
                            Type::getInt32PtrTy(*_context), // initial state pointer
                            Type::getInt32PtrTy(*_context), // mailbox pointer
                            Type::getInt32PtrTy(*_context), // type pointer
                    }, false)
            );

            return builder->CreateCall(fn, {
                    address,
                    supervisor,
                    initial,
                    mb,
                    type,
            });
        }

        inline Value *dlactor(shared_ptr<IRBuilder<>> &builder, Value *actor) {
            auto fn = _module->getOrInsertFunction("dlactor", FunctionType::get(Type::getInt32Ty(*_context), { Type::getInt32Ty(*_context) }, false));
            return builder->CreateCall(fn, actor);
        }

        inline Value *extern_malloc(shared_ptr<IRBuilder<>> &builder, uint32_t size) {
            auto fn = _module->getOrInsertFunction("malloc", FunctionType::get(Type::getInt32Ty(*_context), { Type::getInt32Ty(*_context) }, false));
            return builder->CreateCall(fn, { ConstantInt::get(*_context, APInt(32, size )) });
        }

    private:
        shared_ptr<LLVMContext> _context;
        shared_ptr<Module> _module;
    };

} // runtime
