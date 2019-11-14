sonata
======

How to compile
--------------

To compile you will need a fat-jar or running the io.sonata.lang.cli.Bootstrap class from your IDE with the following
parameters:

```sh
$> <input-files> -o <output>.js 
```

For example, if you want to compile one of the samples, you would do (assuming you are in the root folder of the example):

```sh
$> <snc or java -jar> fibonacci.sn -o fibonacci.js
```

Running the Tests
-----------------

Most Sonata tests are E2E, they get a source file, the compile to a javascript file, run a docker container with
the generated javascript file, and check the output. Tests are in the `io.sonata.lang.e2e` package.

You only need Docker to run the test suite.

Architecture
------------

The sonata compiler is built by five main modules:

* CLI: Exposes a command line interface to the compiler.
* Tokenizer: Process a Sonata source code file and generates meaningful tokens.
* Parser: Process a stream of tokens and build an AST. If needed, imports other Sonata source files into the compiler. The AST is immutable and typesafe.
* Analyzer: Processors that simplify the AST to generate new smarter structures. For example, generates Lambdas from a partial expression.
* Backend: Generates the bytecode or binary for a given platform (only JS supported now).
