#include "backend.h"

#include "llvm/ADT/Optional.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Support/TargetRegistry.h"
#include "llvm/Support/FileSystem.h"
#include "llvm/Support/Host.h"
#include "llvm/Support/TargetSelect.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Target/TargetOptions.h"
#include "llvm/Transforms/InstCombine/InstCombine.h"
#include "llvm/Transforms/Scalar.h"
#include "llvm/Transforms/Scalar/GVN.h"
#include <memory>
#include <system_error>
#include <vector>

#include "intrinsics/intrinsics.h"
#include "type_registry.h"
#include "diagnostic.h"

using namespace llvm;
using namespace llvm::sys;

namespace scc::backend::llvm {
    using scc::type_system::type_registry;
    using namespace intrinsics;

    llvm_backend::llvm_backend(std::shared_ptr<type_registry> &sonata_types) {
        _context = std::make_shared<LLVMContext>();
        _module = std::make_shared<Module>("Sonata", *_context);
        _builder = std::make_shared<IRBuilder<>>(*_context);
        _pass_manager = std::make_shared<legacy::FunctionPassManager>(_module.get());
        _ir_builder = std::make_shared<ir_builder>(
                _context,
                _builder,
                _module,
                _pass_manager,
                sonata_types
        );

        InitializeAllTargetInfos();
        InitializeAllTargets();
        InitializeAllTargetMCs();
        InitializeAllAsmParsers();
        InitializeAllAsmPrinters();

        _pass_manager->add(createAlignmentFromAssumptionsPass());
        _pass_manager->add(createInstructionCombiningPass());
        _pass_manager->add(createDeadCodeEliminationPass());
        _pass_manager->add(createDeadStoreEliminationPass());
        _pass_manager->add(createLoopUnrollPass());
        _pass_manager->add(createFlattenCFGPass());
        _pass_manager->add(createReassociatePass());
        _pass_manager->add(createGVNPass());
        _pass_manager->add(createTailCallEliminationPass());
        _pass_manager->add(createCFGSimplificationPass());

        _pass_manager->doInitialization();

        auto TargetTriple = sys::getDefaultTargetTriple();
        _module->setTargetTriple(TargetTriple);

        std::string Error;
        auto Target = TargetRegistry::lookupTarget(TargetTriple, Error);

        if (!Target) {
            errs() << Error;
            throw std::exception();
        }

        auto CPU = "generic";
        auto Features = "";

        TargetOptions opt;
        auto RM = Optional<Reloc::Model>();
        _target_machine = std::shared_ptr<TargetMachine>(Target->createTargetMachine(TargetTriple, CPU, Features, opt, RM));
        _module->setDataLayout(_target_machine->createDataLayout());
    }

    void llvm_backend::write(const ast_root &document) {
        _ir_builder->build_ir(document);

        auto Filename = "output.o";
        std::error_code EC;
        raw_fd_ostream dest(Filename, EC, sys::fs::OF_None);

        if (EC) {
            errs() << "Could not open file: " << EC.message();
            throw std::exception();
        }

        D_START_PHASE(scc::diagnostic::diagnostic_phase_id::OPTIMIZE_LLVM_IR);
        legacy::PassManager pass;
        auto FileType = CGFT_ObjectFile;

        if (_target_machine->addPassesToEmitFile(pass, dest, nullptr, FileType)) {
            errs() << "TheTargetMachine can't emit a file of this type";
            throw std::exception();
        }

        pass.run(*_module);
        dest.flush();
        D_END_PHASE();

#ifdef DEBUG
        _module->print(errs(), nullptr);
#endif
    }
}

