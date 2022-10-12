package refactroing_base.strategies.rearrange_nodes

import org.antlr.v4.runtime.ParserRuleContext
import refactroing_base.ParserContext
import refactroing_base.TokenRangeUtils
import refactroing_base.TokenRangeUtils.extendTokensToHiddenTokens
import refactroing_base.TokenRangeUtils.getFullText
import java.util.*

object RearrangeNodesRefactoringStrategy {
    fun rearrangeNodes(
        members: List<ParserRuleContext>,
        filterFunctions: List<(ParserRuleContext) -> Boolean>,
        parserContext: ParserContext,
        tokenExtensionConfig: TokenRangeUtils.TokenExtensionConfig = TokenRangeUtils.TokenExtensionConfig()
    ): Boolean {
        var changed = false

        val sortedClassMembers = sort(members, filterFunctions)

        assert(
            sortedClassMembers.size == members.size
        ) { "Size of sorted members (${sortedClassMembers.size}) is not equal to size of class body declarations (${members.size})" }


        for (i in members.indices) {
            val sourceMember = members[i]
            val replacingMember = sortedClassMembers[i]

            if (sourceMember == replacingMember) continue

            val (start, end) = extendTokensToHiddenTokens(
                sourceMember.start,
                sourceMember.stop,
                parserContext.tokenStream,
                tokenExtensionConfig
            )

            changed = true
            parserContext.rewriter.replace(
                start, end,
                getFullText(replacingMember, parserContext.tokenStream, tokenExtensionConfig)
            )

        }

        return changed
    }

    fun sort(
        members: List<ParserRuleContext>, filterFunctions: List<(ParserRuleContext) -> Boolean>
    ): List<ParserRuleContext> {
        val sortedClassMembers: List<MutableList<ParserRuleContext>> = List(filterFunctions.size + 1) { LinkedList() }

        member@ for (member in members) {
            for ((index, filterFn) in filterFunctions.withIndex()) {
                if (filterFn(member)) {
                    sortedClassMembers[index].add(member)
                    continue@member
                }
            }
            sortedClassMembers.last().add(member)
        }

        return sortedClassMembers.flatten()
    }
}
