package refactroing_base.strategies.replace_source_node_with_target_node

import org.antlr.v4.runtime.tree.ParseTree

data class RuleMatcherConditions(
    val sourceCondition: (ParseTree) -> Boolean,
    val processingChildSelector: (ParseTree) -> ParseTree,
    val matchCondition: (ParseTree) -> Boolean,
    val cancelCondition: (ParseTree) -> Boolean
)
