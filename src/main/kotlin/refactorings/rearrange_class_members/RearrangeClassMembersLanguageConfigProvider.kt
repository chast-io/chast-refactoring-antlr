package refactorings.rearrange_class_members

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

interface RearrangeClassMembersLanguageConfigProvider<ClassBody : ParseTree, Declaration : ParserRuleContext> {
    fun getConfig(): RearrangeClassMembersConfig<ClassBody, Declaration>
}
