# [CHAST] Refactoring ANTLR

Refactoring based on [ANTLR](https://www.antlr.org/) and its [grammars](https://github.com/antlr/grammars-v4).

## Structure

* `base`: Base operations for running refactorings on the basis of ANTLR
  * `java`: Base Classes required by some of the lexers and parsers.
  * `kotlin`: Actual code
    * `strategies`: Refactoring strategies for doing certain operations like rearranging or replacing nodes
    * This is also the entry point for loading the different lexers/parsers.
* `refactorings`: Home of all the refactorings currently implemented
  * `rearrange_class_members`: Refactoring for rearranging class members according to a user specified configuration.
    * *Currently supported languages: Java, Kotlin, C#*
    * A chast recipe is available with some tests available. Check the [main repository](https://github.com/chast-io/chast-core) for a demo.
  * `remove_double_negation`: Refactoring for eliminating double negations like `if(!!isEmpty) -> if(isEmpty)`
    * *Currently supported languages: Java, Python*
    * A chast recipe is available with some tests available. Check the [main repository](https://github.com/chast-io/chast-core) for a demo.
