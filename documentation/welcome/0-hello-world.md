# Hello World

Sonata source code is structured in folders and files, that act as limitation of module boundaries. 
In this repository there is already a folder sn_examples that contains a few example modules that can
be compiled and played with.

A Hello World application Sonata looks like:

```sn
let extern printf(text: string): none
printf('Hello World')
```

With the expected outcome:

```Hello World```

This program shows some basic syntax blocks in Sonata. Let's go line by line:

```sn
let extern printf(text: string): nothing
```

Let expressions allow to define functions in Sonata. Let functions marked as "extern" are external
native code that will be linked during compilation time. In this example, we declare that we depend
on the standard's library printf function.

Functions can have parameters: in this case, the text parameter that is a string. For most common cases,
the compiler is smart enough to transform from Sonata types to C types by itself. Custom mappings can be
defined, but are out of scope of this tutorial.

Functions have a mandatory return type. In this example, the type `nothing`. The type `nothing` can be though
as a the `void` type in other languages. `nothing` means that the function would not return anything important.

```sn
printf('Hello World')
```

In Sonata, all code in the global scope is considered code to be executed at the start of the program. Sonata only
guarantees that code in the same file will be executed in order. Code in between files do not necessarily need to be executed
in order and global variables can not be shared (they are essentially private to the file).