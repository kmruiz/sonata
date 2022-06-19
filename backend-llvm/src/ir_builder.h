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

#include "ast.h"

namespace scc::backend::llvm {
    using namespace ::llvm;
    using namespace scc::ast;
    using ::llvm::legacy::FunctionPassManager;
    using std::shared_ptr;
    using scc::ast::node_ref;
    using std::shared_ptr;
    using std::make_shared;
    using std::map;
    using std::string;
    using std::optional;
    using std::shared_ptr;


    class ir_builder {
    public:
        ir_builder(
                shared_ptr<LLVMContext> &context,
                shared_ptr<IRBuilder<>> &ir,
                shared_ptr<Module> &module,
                shared_ptr<FunctionPassManager> &pass_manager
        );

        ~ir_builder() = default;
        void build_ir(const scc::ast::ast_root &document);
    private: // IR generation methods
        Value *to_value(const expression_ref &expr);
        Value *to_value(const shared_ptr<ast::nfunction_call> &expr);
        Value *to_value(const shared_ptr<ast::nconstant> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_malloc> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_free> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_function_call> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_bitbag_set> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_direct_set> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_bitbag_get> &expr);
        Value *to_value(const shared_ptr<ast::ir::nstruct_direct_get> &expr);
        void register_extern_function(const shared_ptr<ast::nlet_function> &letfn);
        void register_function(const shared_ptr<ast::nlet_function> &letfn);
        void register_function(const shared_ptr<ast::ir::nstruct_function_def> &letfn);
        void register_struct(const shared_ptr<ast::ir::nstruct> &nstruct);
    private: // private state
        shared_ptr<LLVMContext> _context;
        shared_ptr<IRBuilder<>> _builder;
        shared_ptr<Module> _module;
        shared_ptr<FunctionPassManager> _pass_manager;
        map<string, AllocaInst *> _locals;
        map<string, Value *> _params;
        map<string, Type *> _types;
    };
}

