import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

data class RearrangeClassMembersLanguageProcessor<ClassBody : ParseTree, Declaration : ParserRuleContext>(
    val classBodyMatcher: (ParseTree) -> Boolean,
    val classMembersSelector: (ClassBody) -> List<ParserRuleContext>,
    val declarationTypeFilters: List<(Declaration) -> Boolean>,
    val tokenExtensionConfig: TokenRangeUtils.TokenExtensionConfig
)
