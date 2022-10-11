package refactorings.rearrange_class_members

import JavaLexer
import JavaParser
import JavaParser.ClassBodyContext
import JavaParser.ClassBodyDeclarationContext

internal object RearrangeClassMembersJavaConfigProvider :
    RearrangeClassMembersLanguageConfigProvider<ClassBodyContext, ClassBodyDeclarationContext> {

    override fun getConfig(): RearrangeClassMembersConfig<ClassBodyContext, ClassBodyDeclarationContext> {
        val visibilitySortedFieldFilters = visibilityExtension(
            ::isFieldMember,
            listOf(::isPublic, ::isProtected, ::isPackagePrivate, ::isPrivate)
        ).toTypedArray()

        val visibilitySortedStaticFilters = visibilityExtension(
            ::isStaticBlockMember,
            listOf(::isPublic, ::isProtected, ::isPackagePrivate, ::isPrivate)
        ).toTypedArray()

        val visibilitySortedConstructorFilters = visibilityExtension(
            ::isConstructorMember,
            listOf(::isPublic, ::isProtected, ::isPackagePrivate, ::isPrivate)
        ).toTypedArray()

        val visibilitySortedMembersFilters = visibilityExtension(
            ::isMethodMember,
            listOf(::isPublic, ::isProtected, ::isPackagePrivate, ::isPrivate)
        ).toTypedArray()


        return RearrangeClassMembersConfig(
            classBodyMatcher = { it is ClassBodyContext },
            classMembersSelector = { it.classBodyDeclaration() },
            declarationTypeFilters = listOf(
                *visibilitySortedFieldFilters,
                *visibilitySortedStaticFilters,
                *visibilitySortedConstructorFilters,
                *visibilitySortedMembersFilters,
            )
        )


    }

    private fun visibilityExtension(
        nested: (ClassBodyDeclarationContext) -> Boolean,
        order: List<(ClassBodyDeclarationContext) -> Boolean>
    ): List<(ClassBodyDeclarationContext) -> Boolean> {
        return order.map { selector -> { selector(it) && nested(it) } }
    }

    private fun isPublic(it: ClassBodyDeclarationContext): Boolean {
        return it.modifier().any { it.classOrInterfaceModifier()?.getToken(JavaLexer.PUBLIC, 0) != null }
    }

    private fun isProtected(it: ClassBodyDeclarationContext): Boolean {
        return it.modifier().any { it.classOrInterfaceModifier()?.getToken(JavaLexer.PROTECTED, 0) != null }
    }

    private fun isPackagePrivate(it: ClassBodyDeclarationContext): Boolean {
        return it.modifier().all {
            it.classOrInterfaceModifier()?.let {
                return@let it.getToken(JavaLexer.PUBLIC, 0) == null
                        && it.getToken(JavaLexer.PROTECTED, 0) == null
                        && it.getToken(JavaLexer.PRIVATE, 0) == null
            } ?: false
        }
    }

    private fun isPrivate(it: ClassBodyDeclarationContext): Boolean {
        return it.modifier().any { it.classOrInterfaceModifier()?.getToken(JavaLexer.PRIVATE, 0) != null }
    }

    private fun isStaticBlockMember(it: ClassBodyDeclarationContext) =
        it.block() != null

    private fun isConstructorMember(it: ClassBodyDeclarationContext): Boolean {
        it.memberDeclaration()?.getChild(0)
            .let { return it is JavaParser.ConstructorDeclarationContext || it is JavaParser.GenericConstructorDeclarationContext }
    }

    private fun isMethodMember(it: ClassBodyDeclarationContext): Boolean {
        it.memberDeclaration()?.getChild(0)
            .let { return it is JavaParser.MethodDeclarationContext || it is JavaParser.GenericMethodDeclarationContext }
    }

    private fun isFieldMember(it: ClassBodyDeclarationContext) =
        it.memberDeclaration()?.getChild(0) is JavaParser.FieldDeclarationContext

}
