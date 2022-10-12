package refactorings.rearrange_class_members.languages

import CSharpLexer
import CSharpParser.Class_bodyContext
import CSharpParser.Class_member_declarationContext
import org.antlr.v4.runtime.Lexer
import refactorings.rearrange_class_members.RearrangeClassMembersLanguageProcessor
import refactorings.rearrange_class_members.RearrangeClassMembersLanguageProcessorProvider
import refactorings.rearrange_class_members.config_file.RearrangeClassMembersConfig
import refactroing_base.TokenRangeUtils

internal object RearrangeClassMembersCSharpProcessorProvider :
    RearrangeClassMembersLanguageProcessorProvider<Class_bodyContext, Class_member_declarationContext>() {

    private val memberFilterMap = mapOf(
        "field" to ::isField,
        "constant" to ::isConstant,
        "property" to ::isProperty,
        "method" to ::isMethod,
        "event" to ::isEvent,
        "operator" to ::isOperator,
        "indexer" to ::isIndexer,
        "constructor" to ::isConstructor,
        "finalizer" to ::isFinalizer,
        "deconstructor" to ::isDestructor,
        "composite_nested_type" to ::isNestedType,
        "class" to ::isClass,
        "struct" to ::isStruct,
        "interface" to ::isInterface,
        "enum" to ::isEnum,
        "delegate" to ::isDelegate,
    )

    private val visibilityMap = mapOf(
        "public" to ::isPublic,
        "protected" to ::isProtected,
        "internal" to ::isInternal,
        "package_private" to ::isProtectedInternal,
        "private" to ::isPrivate,
        "private_internal" to ::isPrivateInternal,
    )

    override fun getProcessor(config: RearrangeClassMembersConfig): RearrangeClassMembersLanguageProcessor<Class_bodyContext, Class_member_declarationContext> {
        val declarationTypeFilters = getDeclarationTypeFilters(
            config,
            "c#",
            memberFilterMap,
            visibilityMap
        )


        return RearrangeClassMembersLanguageProcessor(
            classBodyMatcher = { it is Class_bodyContext },
            classMembersSelector = { it.class_member_declarations().class_member_declaration() },
            declarationTypeFilters = declarationTypeFilters,
            tokenExtensionConfig = TokenRangeUtils.TokenExtensionConfig(
                channelsToIncludeBefore = listOf(CSharpLexer.HIDDEN, CSharpLexer.COMMENTS_CHANNEL),
                channelsToIncludeAfter = listOf(Lexer.HIDDEN),
                includeWhitespaceSymbolsBefore = false,
                includeWhitespaceSymbolsAfter = false,
            )
        )


    }

    // Visibility
    // - https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/classes-and-structs/access-modifiers#summary-table

    private fun isPublic(it: Class_member_declarationContext): Boolean {
        return it.all_member_modifiers()?.all_member_modifier()?.any { it.PUBLIC() != null } ?: false
    }

    private fun isProtected(it: Class_member_declarationContext): Boolean {
        return it.all_member_modifiers()?.all_member_modifier()?.any { it.PROTECTED() != null } ?: false
    }

    private fun isInternal(it: Class_member_declarationContext): Boolean {
        return (it.all_member_modifiers()?.all_member_modifier()?.any { it.INTERNAL() != null }
            ?: false) && (it.all_member_modifiers()?.all_member_modifier()
            ?.none { it.PROTECTED() != null || it.PRIVATE() != null } ?: false)
    }

    private fun isProtectedInternal(it: Class_member_declarationContext): Boolean {
        return (it.all_member_modifiers()?.all_member_modifier()?.any { it.PROTECTED() != null }
            ?: false) && (it.all_member_modifiers()?.all_member_modifier()?.any { it.INTERNAL() != null } ?: false)
    }

    private fun isPrivate(it: Class_member_declarationContext): Boolean {
        return it.all_member_modifiers()?.all_member_modifier()?.any { it.PRIVATE() != null } ?: false
    }

    private fun isPrivateInternal(it: Class_member_declarationContext): Boolean {
        return (it.all_member_modifiers()?.all_member_modifier()?.any { it.PRIVATE() != null }
            ?: false) && (it.all_member_modifiers()?.all_member_modifier()?.any { it.INTERNAL() != null } ?: false)
    }

    // Types
    //  - https://learn.microsoft.com/en-us/dotnet/csharp/programming-guide/classes-and-structs/members

    private fun isField(it: Class_member_declarationContext) =
        it.common_member_declaration()?.typed_member_declaration()?.field_declaration() != null

    private fun isConstant(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.constant_declaration() != null

    private fun isProperty(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.typed_member_declaration()?.property_declaration() != null

    private fun isMethod(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.method_declaration() != null || it.common_member_declaration()
            ?.typed_member_declaration()?.method_declaration() != null

    private fun isEvent(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.event_declaration() != null

    private fun isOperator(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.typed_member_declaration()?.operator_declaration() != null

    private fun isIndexer(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.typed_member_declaration()?.indexer_declaration() != null

    private fun isConstructor(it: Class_member_declarationContext): Boolean =
        it.common_member_declaration()?.constructor_declaration() != null

    private fun isFinalizer(it: Class_member_declarationContext) = isDestructor(it)
    private fun isDestructor(it: Class_member_declarationContext) = it.destructor_definition() != null

    // Subtypes
    private fun isNestedType(it: Class_member_declarationContext): Boolean =
        isClass(it) || isStruct(it) || isInterface(it) || isEnum(it) || isDelegate(it)

    private fun isClass(it: Class_member_declarationContext) =
        it.common_member_declaration()?.class_definition() != null

    private fun isStruct(it: Class_member_declarationContext) =
        it.common_member_declaration()?.struct_definition() != null

    private fun isInterface(it: Class_member_declarationContext) =
        it.common_member_declaration()?.interface_definition() != null

    private fun isEnum(it: Class_member_declarationContext) =
        it.common_member_declaration()?.enum_definition() != null

    private fun isDelegate(it: Class_member_declarationContext) =
        it.common_member_declaration()?.delegate_definition() != null

}
