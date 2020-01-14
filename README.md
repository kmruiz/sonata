sonata
======

If you want information about the syntax of the language, we have the [language wiki](https://github.com/kmruiz/sonata/wiki/1.-Introduction) which provides
a short summary, based on examples, of the language capabilities.

How to use
--------------

### Using Docker

The easiest way to start using Sonata is using one of the provided Docker images that are deployed in dockerhub. There are
two images:

#### Using the playground

The playground is a docker image that contains both the latest tested version of nodejs, and the Sonata compiler, so you can compile and run your code
with a single command. 

Create a Sonata file named hello-world.sn (Sonata file extensions are .sn) in your current directory. Write the following code inside:

```
requires std.io

println('Hello World!')
```

Now run the following command, from the same directory:

```
$> docker run -it -v "$(pwd):/code" kmruiz/sonata:playground sne playground.sn
```

And you will see the following output:

`Hello World!`

#### Using the compiler

We are providing a [a image scratch image with the static binary inside](Dockerfile) so you can run the compiler easily.
There is already a version in dockerhub so you don't need to build the compiler yourself or to prepare a development environment.

Create a file in your current directory, named hello-world.sn, and write inside:

```
requires std.io

println('Hello World!')
```

Now run the compiler:

```
$> docker run -it -v "$(pwd):/code" kmruiz/sonata snc hello-world.sn -o hello-world.js
``` 

It will generate a hello-world.js JavaScript image with your code, that you can run with node:

```
$> node hello-world.js
```

And you will see the following output:

`Hello World!`

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
-------------------------

To generate a docker image with snc, just build the multistage Dockerfile in the root directory:

```ps1
$> docker build -t my-snc .
```

This will generate a fat-jar, a native-image and put it into a Docker image named my-snc.