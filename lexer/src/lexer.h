#pragma once

#include <istream>
#include <memory>
#include <list>

#include "token.h"

namespace scc::lexer {
    using std::istream;
    using std::shared_ptr;
    using std::unique_ptr;
    using std::list;

    typedef list<shared_ptr<token>> token_stream;

    class lexer {
    public:
        explicit lexer();
        ~lexer();

        token_stream process(std::istream &istream, const std::string &name);
    };
}
