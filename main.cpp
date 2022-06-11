#include <iostream>
#include <getopt.h>
#include <fstream>

#include "lexer/src/lexer.h"
#include "diagnostic/src/diagnostic.h"
#include "discovery/src/discovery.h"
#include "parser/src/parser.h"
#include "backend-llvm/src/backend.h"
#include "passes/src/pass_manager.h"

int main(int argc, char **argv) {
    bool diagnostic = false;
    std::string diagnostic_file = "scc-diagnostic.json";
    scc::diagnostic::diagnostic_log_level allowed_level = scc::diagnostic::diagnostic_log_level::WARN;
    std::list<std::string> directories_to_process;
    int c;

    while ((c = getopt(argc, argv, "D:L:")) != -1) {
        switch (c) {
            case 'D':
                diagnostic = true;
                if (std::string(optarg) != "default") {
                    diagnostic_file = optarg;
                    if (!diagnostic_file.ends_with(".json")) {
                        diagnostic_file += ".json";
                    }
                }

                break;
            case 'L':
                std::string level = optarg;
                if (level == "DEBUG") {
                    allowed_level = scc::diagnostic::diagnostic_log_level::DEBUG;
                } else if (level == "WARN") {
                    allowed_level = scc::diagnostic::diagnostic_log_level::WARN;
                } else if (level == "INFO") {
                    allowed_level = scc::diagnostic::diagnostic_log_level::INFO;
                } else if (level == "ERROR") {
                    allowed_level = scc::diagnostic::diagnostic_log_level::ERROR;
                } else if (level == "FATAL") {
                    allowed_level = scc::diagnostic::diagnostic_log_level::FATAL;
                }
        }
    }

    for(; optind < argc; optind++){
        directories_to_process.emplace_back(argv[optind]);
    }

    if (directories_to_process.empty()) {
        directories_to_process.emplace_back(".");
    }

    scc::diagnostic::initialize(allowed_level);
    scc::discovery::discovery discovery;
    scc::lexer::lexer lexer;
    scc::parser::parser parser;
    scc::passes::pass_manager pass_manager({}, {});
    scc::backend::llvm::llvm_backend backend;

    std::list<std::string> files_to_process = discovery.discover_source_files(directories_to_process);
    scc::lexer::token_stream all_tokens;

    for (const auto &source_file : files_to_process) {
        std::ifstream ifs(source_file);
        auto processed = lexer.process(ifs, source_file);
        all_tokens.splice(all_tokens.end(), processed);
    }

    auto result = parser.parse(all_tokens);
    pass_manager.run(result);

    backend.write(result);

    scc::diagnostic::finish();
    scc::diagnostic::print_user_diagnostic();

    if (diagnostic) {
        scc::diagnostic::dump_diagnostic(diagnostic_file);
    }

    return scc::diagnostic::return_code();
}
