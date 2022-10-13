rootProject.name = "chast-refactoring-antlr"
include("base")

include("rearrange_class_members")
include("remove_double_negation")

project(":rearrange_class_members").projectDir = file("refactorings/rearrange_class_members")
project(":remove_double_negation").projectDir = file("refactorings/remove_double_negation")
