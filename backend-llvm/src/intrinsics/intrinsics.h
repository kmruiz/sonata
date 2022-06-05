#pragma once

#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Module.h"

namespace scc::backend::llvm::intrinsics {
    using namespace ::llvm;
    using std::shared_ptr;

    class intrinsics {
    public:
        virtual void register_into(shared_ptr<LLVMContext> &context, shared_ptr<Module> &module) = 0;
    };

    class internal_std_io : public intrinsics {
    public:
        void register_into(shared_ptr<LLVMContext> &context, shared_ptr<Module> &module) override;
    };

}