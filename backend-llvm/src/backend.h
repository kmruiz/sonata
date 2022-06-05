#pragma once

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

#include <algorithm>
#include <cassert>
#include <cctype>
#include <cstdio>
#include <cstdlib>
#include <map>
#include <memory>
#include <string>
#include <system_error>
#include <utility>
#include <vector>

#include "ir_builder.h"
#include "ast.h"

namespace scc::backend::llvm {
    using ::llvm::LLVMContext;
    using ::llvm::IRBuilder;
    using ::llvm::Module;
    using ::llvm::TargetMachine;
    using ::llvm::legacy::FunctionPassManager;
    using std::shared_ptr;
    using scc::ast::ast_root;

    class llvm_backend {
    public:
        explicit llvm_backend();
        void write(const ast_root &document);
    private:
        shared_ptr<LLVMContext> _context;
        shared_ptr<IRBuilder<>> _builder;
        shared_ptr<Module> _module;
        shared_ptr<TargetMachine> _target_machine;
        shared_ptr<FunctionPassManager> _pass_manager;
        shared_ptr<ir_builder> _ir_builder;
    };
}


