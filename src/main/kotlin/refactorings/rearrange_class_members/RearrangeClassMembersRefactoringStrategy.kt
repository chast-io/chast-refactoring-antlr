package refactorings.rearrange_class_members

import JavaLexer
import JavaParser
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import refactroing_base.BaseRefactoring
import refactroing_base.ParserFactory
import refactroing_base.RefactoringResponse
import refactroing_base.SupportedLanguage
import java.util.*
import kotlin.streams.asStream

// TODO:  rule of thumb, put methods above all methods called from their body. Static methods cannot call other methods of the class and come last.

object RearrangeClassMembersRefactoringStrategy : BaseRefactoring() {
    const val NO_VISIBILITY_MODIFIER = -1

    override fun processCodeString(code: String, language: SupportedLanguage): RefactoringResponse {
        when (language) {
            SupportedLanguage.JAVA -> {
                val changedCode = walkAndProcess<JavaParser.ClassBodyContext>(
                    code,
                    { c -> ParserFactory.getParseTreeForLanguage(SupportedLanguage.JAVA, c) },
                    { it is JavaParser.ClassBodyContext },
                    { it, parserContext ->
                        var changed = false
                        runBlocking {
                            val sortedMembers = getSortedClassBodyDeclarations(it).flatMap { it.toList() }
                            val singleSemicolons = it.classBodyDeclaration().count { it.SEMI() != null }


                            assert(
                                sortedMembers.size == it.classBodyDeclaration().size - singleSemicolons
                            ) { "Size of sorted members (${sortedMembers.size}) is not equal to size of class body declarations (${it.classBodyDeclaration().size})" }


                            var droppedStatementsCorrectionValue = 0
                            for (i in 0 until it.classBodyDeclaration().size) {
                                val replacingDeclaration = it.classBodyDeclaration()[i]
                                if (replacingDeclaration != sortedMembers[i + droppedStatementsCorrectionValue]) {

                                    if (replacingDeclaration.SEMI() != null) {
                                        val (startToken, endToken) = extendTokensToHiddenTokens(
                                            replacingDeclaration.start,
                                            replacingDeclaration.stop,
                                            parserContext.tokenStream,
                                            includeSpacesSymbolsBefore = false,
                                            includeSpacesSymbolsAfter = true
                                        )
                                        parserContext.rewriter.delete(startToken, endToken)
                                        droppedStatementsCorrectionValue--
                                        continue
                                    }

                                    changed = true
                                    parserContext.rewriter.replace(
                                        replacingDeclaration.start,
                                        replacingDeclaration.stop,
                                        getFullText(
                                            sortedMembers[i + droppedStatementsCorrectionValue] as ParserRuleContext,
                                            parserContext.tokenStream
                                        )
                                    )
                                }
                            }
                        }
                        return@walkAndProcess changed
                    })

                return RefactoringResponse(changedCode, (changedCode != code))
            }

            else -> {
                throw UnsupportedOperationException("Unsupported language: $language")
            }
        }
    }

    private suspend fun getSortedClassBodyDeclarations(classBodyContext: JavaParser.ClassBodyContext): List<List<ParseTree>> =
        coroutineScope {
            /*
            classBodyDeclaration
                : ';'
                | STATIC? block
                | modifier* memberDeclaration
                ;
             */
            val staticBlocks = classBodyContext.classBodyDeclaration()
                .filter { it.block() != null }
                .toList()


            val members = classBodyContext.classBodyDeclaration()
                .filter { it.memberDeclaration() != null }
                .sortedWith(::sortVisibilities)
                .toList()

            val fields = async { getFields(members) }
            val methods = async { getMethods(members) }
            val constructors = async { getConstructors(members) }
            val others = async { getOtherMembers(members) }

//            val staticFields = fields.await().filter { it.modifier().any { it.STATIC() != null } }

            return@coroutineScope listOf(
                fields.await(),
                staticBlocks,
                constructors.await(),
                methods.await(),
                others.await()
            )
        }

    private fun sortVisibilities(
        a: JavaParser.ClassBodyDeclarationContext,
        b: JavaParser.ClassBodyDeclarationContext
    ): Int {
        return getVisibilityValue(a).compareTo(getVisibilityValue(b))
    }

    private fun getVisibilityValue(context: JavaParser.ClassBodyDeclarationContext): Int {
        val modifiers = context.modifier()
            .map { it.classOrInterfaceModifier() }
            .filterIsInstance<JavaParser.ClassOrInterfaceModifierContext>()

        var noVisibilityModifierValue = NO_VISIBILITY_MODIFIER
        for ((index, visibility) in getVisibilitiesInSortingOrder().withIndex()) {
            if (visibility == NO_VISIBILITY_MODIFIER) {
                noVisibilityModifierValue = index
                continue
            }
            if (modifiers.any { it.getToken(visibility, 0) != null }) {
                return index
            }
        }
        return noVisibilityModifierValue
    }

    private fun getVisibilitiesInSortingOrder(): List<Int> {
        return listOf(JavaLexer.PUBLIC, JavaLexer.PROTECTED, NO_VISIBILITY_MODIFIER, JavaLexer.PRIVATE)
    }


    private fun getOtherMembers(members: List<JavaParser.ClassBodyDeclarationContext>) =
        members.filter {
            it.memberDeclaration().getChild(0)
                .let {
                    it !is JavaParser.FieldDeclarationContext &&
                            it !is JavaParser.MethodDeclarationContext &&
                            it !is JavaParser.GenericMethodDeclarationContext &&
                            it !is JavaParser.ConstructorDeclarationContext &&
                            it !is JavaParser.GenericConstructorDeclarationContext
                }
        }.toList()

    private fun getConstructors(members: List<JavaParser.ClassBodyDeclarationContext>) =
        members.filter {
            it.memberDeclaration().getChild(0)
                .let { it is JavaParser.ConstructorDeclarationContext || it is JavaParser.GenericConstructorDeclarationContext }
        }.toList()

    private fun getMethods(members: List<JavaParser.ClassBodyDeclarationContext>) =
        members.filter {
            it.memberDeclaration().getChild(0)
                .let { it is JavaParser.MethodDeclarationContext || it is JavaParser.GenericMethodDeclarationContext }
        }.toList()

    private fun getFields(members: List<JavaParser.ClassBodyDeclarationContext>) =
        members.filter {
            it.memberDeclaration().getChild(0) is JavaParser.FieldDeclarationContext
        }.toList()
}

private fun ParseTree.forEach(filter: (ParseTree) -> Boolean, function: (ParseTree) -> Unit) {
    for (i in 0 until this.childCount) {
        this.getChild(i).let {
            if (filter(it)) function(it)
            it.forEach(filter, function)
        }
    }
}

private fun ParseTree.stream() = this.asSequence().asStream()

private fun ParseTree.asSequence(): Sequence<ParseTree> {
    val stack = Stack<ParseTree>()
    for (i in this.childCount - 1 downTo 0) {
        stack.push(this.getChild(i))
    }


    return generateSequence {
        while (stack.isNotEmpty()) {
            val next = stack.pop()
            for (i in next.childCount - 1 downTo 0) {
                stack.push(next.getChild(i))
            }
            return@generateSequence next
        }
        null
    }
}
