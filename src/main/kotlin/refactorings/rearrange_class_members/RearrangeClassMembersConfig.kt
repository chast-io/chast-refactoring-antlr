package refactorings.rearrange_class_members

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

data class RearrangeClassMembersConfig<ClassBody : ParseTree, Declaration : ParserRuleContext>(
    val classBodyMatcher: (ParseTree) -> Boolean,
    val classMembersSelector: (ClassBody) -> List<Declaration>,
    val declarationTypeFilters: List<(Declaration) -> Boolean>
)
