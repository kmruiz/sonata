#pragma once

#include <map>
#include <list>
#include <string>
#include <memory>
#include <ctime>

namespace scc::diagnostic {
    using std::map;
    using std::list;
    using std::string;
    using std::shared_ptr;
    using std::initializer_list;
    using std::tm;

    enum class diagnostic_phase_id : unsigned char {
        DISCOVERY, LEXER, PARSER, GENERATE_LLVM_IR, OPTIMIZE_LLVM_IR
    };

    typedef unsigned int count_t;

    enum class diagnostic_log_level : unsigned char {
        DEBUG, INFO, WARN, ERROR, FATAL
    };

    struct diagnostic_log_marker {
        string key;
        string value;
    };

    struct diagnostic_log {
        diagnostic_log_level level;
        tm when;

        string message;
        list<diagnostic_log_marker> markers;
    };

    struct diagnostic_phase {
        diagnostic_phase_id id;
        tm start;
        tm end;

        count_t errors;
        count_t warnings;

        list<shared_ptr<diagnostic_log>> logs;
    };

    struct diagnostic {
        diagnostic_log_level allowed_level;
        string scc_version;
        string os_version;

        tm start;
        tm end;

        count_t total_errors;
        count_t total_warnings;

        list<shared_ptr<diagnostic_phase>> phases;
    };

    void initialize(const diagnostic_log_level allowed_level);
    void start_phase(const diagnostic_phase_id phase);
    void end_phase();
    void log(const diagnostic_log_level level, const string &format, const initializer_list<diagnostic_log_marker> markers);
    void finish();

    void dump_diagnostic(const string &where);
    void print_user_diagnostic();
}

inline void D_START_PHASE(scc::diagnostic::diagnostic_phase_id phase) {
    scc::diagnostic::start_phase(phase);
}

inline void D_END_PHASE() {
    scc::diagnostic::end_phase();
}

inline void D_DEBUG(const std::string &fmt, const std::initializer_list<scc::diagnostic::diagnostic_log_marker> markers) {
    scc::diagnostic::log(scc::diagnostic::diagnostic_log_level::DEBUG, fmt, markers);
}

inline void D_INFO(const std::string &fmt, const std::initializer_list<scc::diagnostic::diagnostic_log_marker> markers) {
    scc::diagnostic::log(scc::diagnostic::diagnostic_log_level::INFO, fmt, markers);
}

inline void D_WARN(const std::string &fmt, const std::initializer_list<scc::diagnostic::diagnostic_log_marker> markers) {
    scc::diagnostic::log(scc::diagnostic::diagnostic_log_level::WARN, fmt, markers);
}

inline void D_ERROR(const std::string &fmt, const std::initializer_list<scc::diagnostic::diagnostic_log_marker> markers) {
    scc::diagnostic::log(scc::diagnostic::diagnostic_log_level::ERROR, fmt, markers);
}

inline void D_FATAL(const std::string &fmt, const std::initializer_list<scc::diagnostic::diagnostic_log_marker> markers) {
    scc::diagnostic::log(scc::diagnostic::diagnostic_log_level::FATAL, fmt, markers);
}