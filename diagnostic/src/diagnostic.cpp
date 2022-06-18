#include <nlohmann/json.hpp>
#include <chrono>
#include <iostream>
#include <iomanip>
#include <fstream>

#include "diagnostic.h"

using json = nlohmann::json;

static std::unique_ptr<scc::diagnostic::diagnostic> S_diagnostic;
static std::shared_ptr<scc::diagnostic::diagnostic_phase> S_current_phase;
static const char *TM_FMT = "%OH:%OM:%OS";
static bool had_error = false;

using std::cout;
using std::endl;
using std::put_time;

using namespace std::chrono;

static std::tm now() {
    std::time_t t = std::time(nullptr);
    return *std::localtime(&t);
}

static const char *phase_id_to_string(scc::diagnostic::diagnostic_phase_id id) {
    switch (id) {
        case scc::diagnostic::diagnostic_phase_id::DISCOVERY:
            return "DISCOVERY";
        case scc::diagnostic::diagnostic_phase_id::LEXER:
            return "LEXER";
        case scc::diagnostic::diagnostic_phase_id::PARSER:
            return "PARSER";
        case scc::diagnostic::diagnostic_phase_id::GENERATE_LLVM_IR:
            return "GENERATE_LLVM_IR";
        case scc::diagnostic::diagnostic_phase_id::OPTIMIZE_LLVM_IR:
            return "OPTIMIZE_LLVM_IR";
        case scc::diagnostic::diagnostic_phase_id::PASS_DETECT_CLASSES:
            return "PASS_DETECT_CLASSES";
        case scc::diagnostic::diagnostic_phase_id::PASS_INTERNAL_MODELER:
            return "PASS_INTERNAL_MODELER";
        case scc::diagnostic::diagnostic_phase_id::PASS_VALUE_CLASS_IR_TRANSFORMER:
            return "PASS_VALUE_CLASS_IR_TRANSFORMER";
        case scc::diagnostic::diagnostic_phase_id::PASS_ENTITY_CLASS_IR_TRANSFORMER:
            return "PASS_ENTITY_CLASS_IR_TRANSFORMER";
        case scc::diagnostic::diagnostic_phase_id::EMIT_LLVM:
            return "EMIT_LLVM";
    }

    return "UNKNOWN?";
}

static const char *log_level_to_string(scc::diagnostic::diagnostic_log_level level) {
    switch (level) {
        case scc::diagnostic::diagnostic_log_level::DEBUG:
            return "DEBUG";
        case scc::diagnostic::diagnostic_log_level::INFO:
            return "INFO ";
        case scc::diagnostic::diagnostic_log_level::WARN:
            return "WARN ";
        case scc::diagnostic::diagnostic_log_level::ERROR:
            return "ERROR";
        case scc::diagnostic::diagnostic_log_level::FATAL:
            return "FATAL";
    }

    return "DEBUG";
}

static const char *log_level_to_string_unpad(scc::diagnostic::diagnostic_log_level level) {
    switch (level) {
        case scc::diagnostic::diagnostic_log_level::DEBUG:
            return "DEBUG";
        case scc::diagnostic::diagnostic_log_level::INFO:
            return "INFO";
        case scc::diagnostic::diagnostic_log_level::WARN:
            return "WARN";
        case scc::diagnostic::diagnostic_log_level::ERROR:
            return "ERROR";
        case scc::diagnostic::diagnostic_log_level::FATAL:
            return "FATAL";
    }

    return "DEBUG";
}


namespace scc::diagnostic {
    void initialize(const diagnostic_log_level allowed_level) {
        S_diagnostic = std::make_unique<scc::diagnostic::diagnostic>();

        S_diagnostic->allowed_level = allowed_level;
        S_diagnostic->scc_version = "0.0.1";
        S_diagnostic->os_version = "Linux";
        S_diagnostic->start = now();
        S_diagnostic->total_errors = 0;
        S_diagnostic->total_warnings = 0;
    }

    void start_phase(const diagnostic_phase_id phase) {
        if (S_diagnostic == nullptr) { // this happens on tests, so run on verbose mode
            initialize(diagnostic_log_level::DEBUG);
        }

        S_current_phase = std::make_shared<scc::diagnostic::diagnostic_phase>();
        S_current_phase->id = phase;
        S_current_phase->start = now();
        S_current_phase->errors = 0;
        S_current_phase->warnings = 0;
    }

    void end_phase() {
        S_current_phase->end = now();
        S_diagnostic->total_errors += S_current_phase->errors;
        S_diagnostic->total_warnings += S_current_phase->warnings;
        S_diagnostic->phases.push_back(S_current_phase);
        S_current_phase = nullptr;
    }

    void log(const diagnostic_log_level level, const string &format, const initializer_list<diagnostic_log_marker> markers) {
        if (level >= diagnostic_log_level::ERROR) {
            had_error = true;
        }

        auto log_msg = std::make_shared<diagnostic_log>();
        if (level == diagnostic_log_level::WARN) {
            S_current_phase->warnings++;
        } else if (level == diagnostic_log_level::ERROR || level == diagnostic_log_level::FATAL) {
            S_current_phase->errors++;
        }

        log_msg->when = now();
        log_msg->level = level;
        log_msg->message = format;
        log_msg->markers.assign(markers);
        S_current_phase->logs.push_back(log_msg);
    }

    void finish() {
        S_diagnostic->end = now();
    }

    void dump_diagnostic(const string &where) {
        std::ofstream out(where);

        json dump = json();
        dump["allowed_level"] = log_level_to_string_unpad(S_diagnostic->allowed_level);
        dump["scc_version"] = S_diagnostic->scc_version;
        dump["os_version"] = S_diagnostic->os_version;
        dump["start"] = mktime(&S_diagnostic->start);
        dump["end"] = mktime(&S_diagnostic->end);
        dump["total_errors"] = S_diagnostic->total_errors;
        dump["total_warnings"] = S_diagnostic->total_warnings;
        std::vector<json> phases;
        for (const auto &phase : S_diagnostic->phases) {
            auto pj = json();
            pj["phase"] = phase_id_to_string(phase->id);
            pj["start"] = mktime(&phase->start);
            pj["end"] = mktime(&phase->end);
            pj["errors"] = phase->errors;
            pj["warnings"] = phase->warnings;

            std::vector<json> logs;
            for (const auto &log : phase->logs) {
                json lj = json();
                lj["level"] = log_level_to_string_unpad(log->level);
                lj["when"] = mktime(&log->when);
                lj["message"] = log->message;

                std::vector<json> markers;
                for (const auto &marker : log->markers) {
                    json mj = json();
                    mj["key"] = marker.key;
                    mj["value"] = marker.value;

                    markers.push_back(mj);
                }

                lj["markers"] = markers;
                logs.push_back(lj);
            }

            pj["logs"] = logs;
            phases.push_back(pj);
        }

        dump["phases"] = phases;
        out << dump.dump(2) << std::endl;
        out.close();
    }

    void print_user_diagnostic() {
        cout << "[" << put_time(&S_diagnostic->start, TM_FMT) << "] GLOBAL     : scc version " << S_diagnostic->scc_version << " and os version " << S_diagnostic->os_version << endl;
        for (const auto &phase : S_diagnostic->phases) {
            auto phase_name = phase_id_to_string(phase->id);

            for (const auto &log : phase->logs) {
                if (log->level >= S_diagnostic->allowed_level) {
                    cout << "[" << put_time(&log->when, TM_FMT) << "] " << log_level_to_string(log->level) << " " << phase_name << ": " << log->message << " | ";
                    for (const auto &marker : log->markers) {
                        cout << marker.key << "=" << marker.value << " | ";
                    }
                    cout << endl;
                }
            }
        }

        auto end = mktime(&S_diagnostic->end);
        auto start = mktime(&S_diagnostic->start);

        auto seconds = (unsigned long) difftime(end, start);
        cout << "[" << put_time(&S_diagnostic->start, TM_FMT) << "] GLOBAL     : Compilation finished ";

        if (S_diagnostic->total_errors > 0) {
            cout << "with " << S_diagnostic->total_errors << " errors ";

            if (S_diagnostic->total_warnings > 0) {
                cout << "and ";
            }
        }

        if (S_diagnostic->total_warnings > 0) {
            cout << S_diagnostic->total_warnings << " warnings ";
        }

        cout << "in " << seconds << " seconds." << endl;
    }

    int return_code() {
        return had_error ? 1 : 0;
    }
}