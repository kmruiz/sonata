#include "intrinsics.h"

namespace scc::backend::llvm::intrinsics {
    void internal_std_io::register_into(shared_ptr<LLVMContext> &context, shared_ptr<Module> &module) {
        module->getOrInsertFunction("printf",
                                    FunctionType::get(Type::getInt32Ty(*context),
                                                      {
                                                              PointerType::getUnqual(Type::getInt32Ty(*context))
                                                      }, true));
    }
}
