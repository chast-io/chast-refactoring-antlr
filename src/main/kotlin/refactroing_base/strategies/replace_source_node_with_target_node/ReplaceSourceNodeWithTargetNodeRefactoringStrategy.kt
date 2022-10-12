package refactroing_base.strategies.replace_source_node_with_target_node

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import refactroing_base.RefactoringUtils
import refactroing_base.ParserContext
import refactroing_base.RefactoringUtils.walkAndProcess
import refactroing_base.TokenRangeUtils.getFullText
import java.util.*

/**
 * This refactoring strategy replaces the first occurrence of a rule with the second occurrence of the same rule.
 * This is useful for refactoring code that has redundant code.
 */
object ReplaceSourceNodeWithTargetNodeRefactoringStrategy {

    fun replaceFirstWithSecondOccurrence(
        code: String,
        codeToTree: (String) -> ParserContext,
        conditions: RuleMatcherConditions
    ): String {
        return walkAndProcess<ParserRuleContext>(code, codeToTree,
            { it is ParserRuleContext && conditions.sourceCondition(it) },
            { it, parserContext ->
                val child = findChild(conditions.processingChildSelector(it), conditions)
                if (child != null) {
                    parserContext.rewriter.replace(
                        it.start,
                        it.stop,
                        getFullText(child.getChild(1) as ParserRuleContext, parserContext.rewriter.tokenStream)
                    )
                    return@walkAndProcess true
                }
                return@walkAndProcess false
            })
    }


    fun findChild(
        root: ParseTree,
        conditions: RuleMatcherConditions
    ): ParseTree? {
        val queue: Queue<ParseTree> = LinkedList()
        queue.offer(root)

        while (queue.isNotEmpty()) {
            val head = queue.poll()
            if (conditions.cancelCondition(head)) return null
            if (conditions.matchCondition(head)) return head
            for (i in 0 until head.childCount) {
                queue.offer(head.getChild(i))
            }
        }
        return null
    }
}
