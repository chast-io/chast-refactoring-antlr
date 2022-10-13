package languages

import JavaLexer
import JavaParser
import RearrangeClassMembersLanguageProcessor
import RearrangeClassMembersLanguageProcessorProvider
import config_file.RearrangeClassMembersConfig
import TokenRangeUtils

internal object RearrangeClassMembersJavaProcessorProvider :
    RearrangeClassMembersLanguageProcessorProvider<JavaParser.ClassBodyContext, JavaParser.ClassBodyDeclarationContext>() {

    private val memberFilterMap = mapOf(
        "field" to ::isField,
        "static_block" to ::isStaticBlock,
        "constructor" to ::isConstructor,
        "method" to ::isMethod,
    )

    private val visibilityMap = mapOf(
        "public" to ::isPublic,
        "protected" to ::isProtected,
        "package_private" to ::isPackagePrivate,
        "private" to ::isPrivate,
    )


    override fun getProcessor(config: RearrangeClassMembersConfig): RearrangeClassMembersLanguageProcessor<JavaParser.ClassBodyContext, JavaParser.ClassBodyDeclarationContext> {
        val declarationTypeFilters = getDeclarationTypeFilters(
            config,
            "java",
            memberFilterMap,
            visibilityMap
        )

        return RearrangeClassMembersLanguageProcessor(
            classBodyMatcher = { it is JavaParser.ClassBodyContext },
            classMembersSelector = { it.classBodyDeclaration() },
            declarationTypeFilters = declarationTypeFilters,
            tokenExtensionConfig = TokenRangeUtils.TokenExtensionConfig(
                channelsToIncludeBefore = listOf(JavaLexer.HIDDEN),
                channelsToIncludeAfter = listOf(JavaLexer.HIDDEN),
                includeWhitespaceSymbolsBefore = false,
                includeWhitespaceSymbolsAfter = false,
            )
        )
    }

    // Visibility
    private fun isPublic(it: JavaParser.ClassBodyDeclarationContext): Boolean {
        return it.modifier().any { it.classOrInterfaceModifier()?.getToken(JavaLexer.PUBLIC, 0) != null }
    }

    private fun isProtected(it: JavaParser.ClassBodyDeclarationContext): Boolean {
        return it.modifier().any { it.classOrInterfaceModifier()?.getToken(JavaLexer.PROTECTED, 0) != null }
    }

    private fun isPackagePrivate(it: JavaParser.ClassBodyDeclarationContext): Boolean {
        return it.modifier().all {
            it.classOrInterfaceModifier()?.let {
                return@let it.getToken(JavaLexer.PUBLIC, 0) == null
                        && it.getToken(JavaLexer.PROTECTED, 0) == null
                        && it.getToken(JavaLexer.PRIVATE, 0) == null
            } ?: false
        }
    }

    private fun isPrivate(it: JavaParser.ClassBodyDeclarationContext): Boolean {
        return it.modifier().any { it.classOrInterfaceModifier()?.getToken(JavaLexer.PRIVATE, 0) != null }
    }

    // Types
    private fun isStaticBlock(it: JavaParser.ClassBodyDeclarationContext) =
        it.block() != null

    private fun isConstructor(it: JavaParser.ClassBodyDeclarationContext): Boolean {
        it.memberDeclaration()?.getChild(0)
            .let { return it is JavaParser.ConstructorDeclarationContext || it is JavaParser.GenericConstructorDeclarationContext }
    }

    private fun isMethod(it: JavaParser.ClassBodyDeclarationContext): Boolean {
        it.memberDeclaration()?.getChild(0)
            .let { return it is JavaParser.MethodDeclarationContext || it is JavaParser.GenericMethodDeclarationContext }
    }

    private fun isField(it: JavaParser.ClassBodyDeclarationContext) =
        it.memberDeclaration()?.getChild(0) is JavaParser.FieldDeclarationContext

}
