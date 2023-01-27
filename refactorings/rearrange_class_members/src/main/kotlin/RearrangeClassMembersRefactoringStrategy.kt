import config_file.RearrangeClassMembersConfig
import config_file.RearrangeClassMembersConfigReader.readConfig
import languages.RearrangeClassMembersCSharpProcessorProvider
import languages.RearrangeClassMembersJavaProcessorProvider
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import RefactoringUtils.readFile
import RefactoringUtils.walkAndProcess
import strategies.rearrange_nodes.RearrangeNodesRefactoringStrategy.rearrangeNodes
import java.io.File

object RearrangeClassMembersRefactoringStrategy {

    fun processFile(file: File, configFile: File): RefactoringResponse {
        val codeText = readFile(file)
        val config = readConfig(configFile)
        return processCodeString(
            codeText,
            SupportedLanguage.getLanguageFromExtension(file.extension),
            config
        )
    }

    fun processCodeString(
        code: String, language: SupportedLanguage, config: RearrangeClassMembersConfig
    ): RefactoringResponse {
        val configProvider = when (language) {
            SupportedLanguage.Java -> RearrangeClassMembersJavaProcessorProvider
            SupportedLanguage.CSharp -> RearrangeClassMembersCSharpProcessorProvider

            else -> throw UnsupportedOperationException("Unsupported language: $language")
        }

        val codeToTree: (String) -> ParserContext = ({ c -> ParserFactory.getParseTreeForLanguage(language, c) })

        @Suppress("UNCHECKED_CAST")
        val processor =
            configProvider.getProcessor(config) as RearrangeClassMembersLanguageProcessor<ParseTree, ParserRuleContext>

        val changedCode = walkAndProcess<ParserRuleContext>(
            code,
            codeToTree,
            processor.classBodyMatcher
        ) { classBody, parserContext ->
            return@walkAndProcess rearrangeNodes(
                processor.classMembersSelector(classBody),
                processor.declarationTypeFilters,
                parserContext,
                processor.tokenExtensionConfig
            )
        }

        return RefactoringResponse(changedCode, (changedCode != code))
    }
}
