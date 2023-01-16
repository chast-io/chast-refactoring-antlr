# Grammar Notes

## Kotlin
> * Grammar: https://github.com/antlr/grammars-v4/tree/master/kotlin/kotlin
> * Version: Commit 9644ff90b769cecf2ee0089c88993042e401a75e

**IMPORTANT NOTE:** 

This grammar is adjusted to NOT skip whitespaces.
All occurrences of `skip` are replaced with `channel(HIDDEN)`.
