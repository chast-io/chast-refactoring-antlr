import org.antlr.v4.runtime.tree.ParseTree
import strategies.replace_source_node_with_target_node.ReplaceSourceNodeWithTargetNodeRefactoringStrategy.replaceFirstWithSecondOccurrence
import strategies.replace_source_node_with_target_node.RuleMatcherConditions
import java.io.File


object RemoveDoubleNegationRefactoring {

    fun processFile(file: File): RefactoringResponse {
        val codeText = RefactoringUtils.readFile(file)
        return processCodeString(
            codeText,
            SupportedLanguage.getLanguageFromExtension(file.extension),
        )
    }

    fun processCodeString(code: String, language: SupportedLanguage): RefactoringResponse {
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

            else -> throw UnsupportedOperationException("Unsupported language: $language")
        }

        val codeAfterProcessing = replaceFirstWithSecondOccurrence(code, codeToParserContext, conditions)
        return RefactoringResponse(codeAfterProcessing, (code != codeAfterProcessing))
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
