package refactorings.remove_double_negation

import JavaLexer
import JavaParser
import PythonLexer
import PythonParser
import org.antlr.v4.runtime.tree.ParseTree
import refactroing_base.ParserContext
import refactroing_base.ParserFactory
import refactroing_base.RefactoringResponse
import refactroing_base.SupportedLanguage
import refactroing_base.strategies.replace_source_node_with_target_node.ReplaceSourceNodeWithTargetNodeRefactoringStrategy
import refactroing_base.strategies.replace_source_node_with_target_node.RuleMatcherConditions


object RemoveDoubleNegationRefactoring : ReplaceSourceNodeWithTargetNodeRefactoringStrategy() {

    override fun processCodeString(code: String, language: SupportedLanguage): RefactoringResponse {
        val codeToParserContext: (String) -> ParserContext
        val conditions: RuleMatcherConditions
        when (language) {
            SupportedLanguage.PYTHON -> {
                codeToParserContext = ParserFactory.getParserForLanguage(SupportedLanguage.PYTHON)
                conditions = getPythonRuleMatcherConditions()
            }

            SupportedLanguage.JAVA -> {
                codeToParserContext = ParserFactory.getParserForLanguage(SupportedLanguage.JAVA)
                conditions = getJavaRuleMatcherConditions()
            }
        }

        val codeAfterProcessing = replaceFirstWithSecondOccurrence(code, codeToParserContext, conditions)
        return RefactoringResponse(codeAfterProcessing,  (code != codeAfterProcessing))
    }

    private fun getJavaRuleMatcherConditions(): RuleMatcherConditions {
        val matchCondition: (ParseTree) -> Boolean = {
            it is JavaParser.ExpressionContext && it.prefix?.type == JavaLexer.BANG && it.expression().count() == 1
        }
        return RuleMatcherConditions(
            processingChildSelector = { (it as JavaParser.ExpressionContext).expression(0) },
            sourceCondition = matchCondition,
            matchCondition = matchCondition,
            cancelCondition = { node -> node is JavaParser.ExpressionContext && (node.bop?.type.let { it == JavaLexer.OR || it == JavaLexer.AND }) }
        )
    }

    private fun getPythonRuleMatcherConditions(): RuleMatcherConditions {
        return RuleMatcherConditions(
            processingChildSelector = { (it as PythonParser.Logical_testContext).logical_test(0) },
            sourceCondition = { it is PythonParser.Logical_testContext && it.NOT() != null },
            matchCondition = {
                it is PythonParser.Logical_testContext && it.NOT() != null && (
                        it.logical_test(0).childCount == 1 ||
                                (it.logical_test(0).childCount == 2 && it.logical_test(0).NOT() != null)
                        )
            },
            cancelCondition = { node -> node is PythonParser.Logical_testContext && node.op?.type.let { it == PythonLexer.AND || it == PythonLexer.OR } }
        )
    }

}
