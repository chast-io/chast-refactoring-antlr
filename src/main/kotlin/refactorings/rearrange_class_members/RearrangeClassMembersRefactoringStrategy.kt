package refactorings.rearrange_class_members

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import refactroing_base.*
import java.util.*

object RearrangeClassMembersRefactoringStrategy : BaseRefactoring() {
    override fun processCodeString(
        code: String, language: SupportedLanguage
    ): RefactoringResponse {
        val configProvider: RearrangeClassMembersLanguageConfigProvider<*, *>
        val codeToTree: (String) -> ParserContext

        when (language) {
            SupportedLanguage.JAVA -> {
                configProvider = RearrangeClassMembersJavaConfigProvider
                codeToTree = ({ c -> ParserFactory.getParseTreeForLanguage(SupportedLanguage.JAVA, c) })
            }
            else -> throw UnsupportedOperationException("Unsupported language: $language")
        }

        val config = configProvider.getConfig()

        // TODO change ClassBodyContext to something not java specific
        val changedCode = walkAndProcess<JavaParser.ClassBodyContext>(
            code,
            codeToTree,
            config.classBodyMatcher
        ) { classBody, parserContext -> return@walkAndProcess processClass(classBody, parserContext, config) }

        return RefactoringResponse(changedCode, (changedCode != code))
    }

    private fun <ClassBody : ParseTree, Declaration : ParserRuleContext> processClass(
        classBody: ClassBody, parserContext: ParserContext, config: RearrangeClassMembersConfig<ClassBody, Declaration>
    ): Boolean {
        var changed = false

        val classMembers = config.classMembersSelector(classBody)
        val sortedClassMembers = sortMembers(classMembers, config.declarationTypeFilters)

        assert(
            sortedClassMembers.size == classMembers.size
        ) { "Size of sorted members (${sortedClassMembers.size}) is not equal to size of class body declarations (${classMembers.size})" }


        for (i in classMembers.indices) {
            val sourceMember = classMembers[i]
            val replacingMember = sortedClassMembers[i]

            if (sourceMember == replacingMember) continue

            changed = true
            parserContext.rewriter.replace(
                sourceMember.start, sourceMember.stop, getFullText(
                    replacingMember, parserContext.tokenStream
                )
            )

        }

        return changed
    }

    private fun <T : ParseTree> sortMembers(
        classMembers: List<T>, declarationTypeFilters: List<(T) -> Boolean>
    ): List<T> {
        val sortedClassMembers: List<MutableList<T>> = List(declarationTypeFilters.size + 1) { LinkedList() }

        member@ for (member in classMembers) {
            for ((index, filterFn) in declarationTypeFilters.withIndex()) {
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
