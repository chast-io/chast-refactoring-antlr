package refactorings.rearrange_class_members

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import refactorings.rearrange_class_members.config_file.RearrangeClassMembersConfig
import refactorings.rearrange_class_members.config_file.RearrangeClassMembersConfigReader
import refactorings.rearrange_class_members.config_file.RearrangeClassMembersConfigReader.readConfig
import refactorings.rearrange_class_members.languages.RearrangeClassMembersCSharpProcessorProvider
import refactorings.rearrange_class_members.languages.RearrangeClassMembersJavaProcessorProvider
import refactroing_base.ParserContext
import refactroing_base.ParserFactory
import refactroing_base.RefactoringResponse
import refactroing_base.RefactoringUtils.readFile
import refactroing_base.RefactoringUtils.walkAndProcess
import refactroing_base.SupportedLanguage
import refactroing_base.strategies.rearrange_nodes.RearrangeNodesRefactoringStrategy.rearrangeNodes
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
            SupportedLanguage.JAVA -> RearrangeClassMembersJavaProcessorProvider
            SupportedLanguage.CSharp -> RearrangeClassMembersCSharpProcessorProvider

            else -> throw UnsupportedOperationException("Unsupported language: $language")
        }

        val codeToTree: (String) -> ParserContext = ({ c -> ParserFactory.getParseTreeForLanguage(language, c) })

        @Suppress("UNCHECKED_CAST")
        val config =
            configProvider.getProcessor(config) as RearrangeClassMembersLanguageProcessor<ParseTree, ParserRuleContext>

        val changedCode = walkAndProcess<ParserRuleContext>(
            code,
            codeToTree,
            config.classBodyMatcher
        ) { classBody, parserContext ->
            return@walkAndProcess rearrangeNodes(
                config.classMembersSelector(classBody),
                config.declarationTypeFilters,
                parserContext,
                config.tokenExtensionConfig
            )
        }

        return RefactoringResponse(changedCode, (changedCode != code))
    }
}

fun main(args: Array<String>) {

    if(args.size != 2) {
        println("Usage: java -jar RearrangeClassMembers.jar <path_to_file> <path_to_config_file>")
        return
    }

    RearrangeClassMembersRefactoringStrategy.processFile(
        args[0].let { File(it) },
        args[1].let { File(it) },
    ).let {
        println(it)
    }
}
