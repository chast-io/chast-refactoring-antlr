package refactorings.rearrange_class_members

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import refactroing_base.TokenRangeUtils

data class RearrangeClassMembersLanguageProcessor<ClassBody : ParseTree, Declaration : ParserRuleContext>(
    val classBodyMatcher: (ParseTree) -> Boolean,
    val classMembersSelector: (ClassBody) -> List<ParserRuleContext>,
    val declarationTypeFilters: List<(Declaration) -> Boolean>,
    val tokenExtensionConfig: TokenRangeUtils.TokenExtensionConfig
)
