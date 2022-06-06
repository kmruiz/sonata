#include <iostream>
#include <getopt.h>
#include <fstream>

#include "lexer/src/lexer.h"
#include "diagnostic/src/diagnostic.h"
#include "discovery/src/discovery.h"
#include "ast/src/node.h"
#include "parser/src/parser.h"
#include "backend-llvm/src/backend.h"
#include "passes/src/pass_manager.h"

int main(int argc, char **argv) {
    bool diagnostic = false;
    std::string diagnostic_file = "scc-diagnostic.json";
    scc::diagnostic::diagnostic_log_level allowed_level = scc::diagnostic::diagnostic_log_level::WARN;
    std::list<std::string> directories_to_process;
    int c;

    while ((c = getopt(argc, argv, "D:L::")) != -1) {
        switch (c) {
            case 'D':
                diagnostic = true;
                std::cout << optarg << std::endl;
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
        directories_to_process.push_back(argv[optind]);
    }

    scc::diagnostic::initialize(allowed_level);
    scc::discovery::discovery discovery;
    scc::lexer::lexer lexer;
    scc::parser::parser parser;
    scc::passes::pass_manager pass_manager({}, {});
    scc::backend::llvm::llvm_backend backend;

    if (directories_to_process.empty()) {
        directories_to_process.emplace_back(".");
    }

    std::list<std::string> files_to_process = discovery.discover_directories(directories_to_process);
    scc::lexer::token_stream all_tokens;

    for (const auto &source_file : files_to_process) {
        std::ifstream ifs(source_file);
        auto processed = lexer.process(ifs, source_file);
        all_tokens.splice(all_tokens.end(), processed);
    }

    backend.write(parser.parse(all_tokens));

    scc::diagnostic::finish();
    scc::diagnostic::print_user_diagnostic();

    if (diagnostic) {
        scc::diagnostic::dump_diagnostic(diagnostic_file);
    }

    return 0;
}
