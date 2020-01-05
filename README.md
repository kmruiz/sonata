sonata
======

How to compile
--------------

To compile you will need a fat-jar or running the io.sonata.lang.Bootstrap class from your IDE with the following
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

Most Sonata tests are E2E:
 
* Read a source file
* Compile to a minified javascript file
* Create a GraalVM context
* Run the compiled script into the GraalVM context
* Check the output.
 
Tests are in the `io.sonata.lang.e2e` package.

Architecture
------------

The sonata compiler is built by five main modules:

* CLI: Exposes a command line interface to the compiler.
* Tokenizer: Process a Sonata source code file and generates meaningful tokens.
* Parser: Process a stream of tokens and build an AST. If needed, imports other Sonata source files into the compiler. The AST is immutable and typesafe.
* Analyzer: Processors that simplify the AST to generate new smarter structures. For example, generates Lambdas from a partial expression.
* Backend: Generates the bytecode or binary for a given platform (only JS supported now).

Generating a Docker image
-----------

To generate a docker image with snc on Windows, just go the build directory and run docker.ps1:

```ps1
$> cd build
$> ./docker.ps1
```

On Linux or OSX, run the following commands:

```sh
$> docker build -t snc .
```

This will generate a fat-jar, a native-image and put it into a Docker image named snc.

How to use
--------------

The easiest way to try the compiler is to use the samples found in the samples directory. For example, to compile the 
fibonacci example, go to the fibonacci directory and run the compiler:

### Windows
```ps1
$> cd samples/fibonacci/
$> docker run -it -v "$(pwd):/code/" -w "/code" kmruiz/sonata snc fibonacci.sn -o fibfromdocker.js
```

### Linux / OSX
```sh
$> cd samples/fibonacci/
$> docker run -it -v "`pwd`:/code/" -w "/code" kmruiz/sonata snc fibonacci.sn -o fibfromdocker.js
```
