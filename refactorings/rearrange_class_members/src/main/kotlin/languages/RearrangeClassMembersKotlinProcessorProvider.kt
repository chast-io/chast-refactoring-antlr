package languages

import KotlinLexer
import KotlinParser
import RearrangeClassMembersLanguageProcessor
import RearrangeClassMembersLanguageProcessorProvider
import TokenRangeUtils
import config_file.RearrangeClassMembersConfig

internal object RearrangeClassMembersKotlinProcessorProvider :
    RearrangeClassMembersLanguageProcessorProvider<KotlinParser.ClassBodyContext, KotlinParser.ClassMemberDeclarationContext>() {

    private val memberFilterMap = mapOf(
        "class" to ::isClass,
        "function" to ::isFunction,
        "object" to ::isObject,
        "companionObject" to ::isCompanionObject,
        "property" to ::isProperty,
        "anonymousInitializer" to ::isAnonymousInitializer,
        "secondaryConstructor" to ::isSecondaryConstructor,
        "typeAlias" to ::isTypeAlias,
    )

    private val visibilityMap = mapOf(
        "public" to ::isPublic,
        "protected" to ::isProtected,
        "internal" to ::isInternal,
        "private" to ::isPrivate,
    )


    override fun getProcessor(config: RearrangeClassMembersConfig): RearrangeClassMembersLanguageProcessor<KotlinParser.ClassBodyContext, KotlinParser.ClassMemberDeclarationContext> {
        val declarationTypeFilters = RearrangeClassMembersKotlinProcessorProvider.getDeclarationTypeFilters(
            config,
            "kotlin",
            memberFilterMap,
            visibilityMap
        )

        return RearrangeClassMembersLanguageProcessor(
            classBodyMatcher = { it is KotlinParser.ClassBodyContext },
            classMembersSelector = { it.classMemberDeclaration() },
            declarationTypeFilters = declarationTypeFilters,
            tokenExtensionConfig = TokenRangeUtils.TokenExtensionConfig(
                channelsToIncludeBefore = listOf(KotlinLexer.HIDDEN),
                channelsToIncludeAfter = listOf(KotlinLexer.HIDDEN),
                includeWhitespaceSymbolsBefore = false,
                includeWhitespaceSymbolsAfter = false,
            )
        )
    }

    // Visibility

    private fun getVisibilityModifiers(member: KotlinParser.ClassMemberDeclarationContext): List<KotlinParser.ModifierContext> {
        return member.classDeclaration()?.modifierList()?.modifier() ?:
        member.functionDeclaration()?.modifierList()?.modifier() ?:
        member.objectDeclaration()?.modifierList()?.modifier() ?:
        member.companionObject()?.modifierList()?.flatMap { it.modifier() } ?:
        member.propertyDeclaration()?.modifierList()?.modifier() ?:
        member.secondaryConstructor()?.modifierList()?.modifier() ?:
        member.typeAlias()?.modifierList()?.modifier() ?:
        emptyList()
    }

    private fun hasVisibilityModifier(
        it: KotlinParser.ClassMemberDeclarationContext,
        selectorFn: (KotlinParser.VisibilityModifierContext?) -> Boolean
    ): Boolean {
        return getVisibilityModifiers(it).any {
            selectorFn(it.visibilityModifier())
        }
    }

    private fun isPublic(it: KotlinParser.ClassMemberDeclarationContext): Boolean {
        return hasVisibilityModifier(it) { it?.PUBLIC() != null } ||
                getVisibilityModifiers(it).isEmpty()
    }

    private fun isProtected(it: KotlinParser.ClassMemberDeclarationContext): Boolean {
        return hasVisibilityModifier(it) { it?.PROTECTED() != null }
    }

    private fun isInternal(it: KotlinParser.ClassMemberDeclarationContext): Boolean {
        return hasVisibilityModifier(it) { it?.INTERNAL() != null }
    }

    private fun isPrivate(it: KotlinParser.ClassMemberDeclarationContext): Boolean {
        return hasVisibilityModifier(it) { it?.PRIVATE() != null }
    }

    // Types
    private fun isClass(it: KotlinParser.ClassMemberDeclarationContext) = it.classDeclaration() != null

    private fun isFunction(it: KotlinParser.ClassMemberDeclarationContext) = it.functionDeclaration() != null

    private fun isObject(it: KotlinParser.ClassMemberDeclarationContext) = it.objectDeclaration() != null

    private fun isCompanionObject(it: KotlinParser.ClassMemberDeclarationContext) = it.companionObject() != null

    private fun isProperty(it: KotlinParser.ClassMemberDeclarationContext) = it.propertyDeclaration() != null

    private fun isAnonymousInitializer(it: KotlinParser.ClassMemberDeclarationContext) =
        it.anonymousInitializer() != null

    private fun isSecondaryConstructor(it: KotlinParser.ClassMemberDeclarationContext) =
        it.secondaryConstructor() != null

    private fun isTypeAlias(it: KotlinParser.ClassMemberDeclarationContext) = it.typeAlias() != null


}
