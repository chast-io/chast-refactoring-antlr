package refactroing_base

import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.misc.Interval
import org.antlr.v4.runtime.tree.ParseTree
import java.io.File

abstract class BaseRefactoring {

    open fun processFile(file: File): RefactoringResponse {
        val codeText = readFile(file)
        return processCodeString(
            codeText,
            SupportedLanguage.getLanguageFromExtension(file.extension)
        )
    }

    abstract fun processCodeString(code: String, language: SupportedLanguage): RefactoringResponse

    protected fun readFile(file: File): String {
        if (!file.exists()) throw IllegalArgumentException("File does not exist at '${file.absolutePath}'")
        return file.readText()
    }

    protected fun <T : ParseTree> walkAndProcess(
        code: String,
        codeToTree: (String) -> ParserContext,
        matchCondition: (ParseTree) -> Boolean,
        processor: (T, ParserContext) -> Boolean
    ): String {
        val parserContext = codeToTree(code)
        walkAndProcessOncePerPath(parserContext.parseTreeRoot, parserContext, matchCondition, processor)

        val alteredCode = parserContext.rewriter.text
        return if (alteredCode != code) {
            walkAndProcess(alteredCode, codeToTree, matchCondition, processor)
        } else {
            alteredCode
        }
    }

    protected fun <T : ParseTree> walkAndProcessOncePerPath(
        root: ParseTree,
        parserContext: ParserContext,
        matchCondition: (ParseTree) -> Boolean,
        processor: (T, ParserContext) -> Boolean
    ) {
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            @Suppress("UNCHECKED_CAST")
            if (matchCondition(child) && processor(child as T, parserContext)) continue
            walkAndProcessOncePerPath(child, parserContext, matchCondition, processor)
        }
    }

    /**
     * https://stackoverflow.com/a/58719524/7893818
     * Explanation: Get the first token, get the last token, and get the text from the input stream between the first char of the first token and the last char of the last token.
     */
    protected fun getFullText(
        context: ParserRuleContext, tokenStream: TokenStream,
        includeSpacesSymbolsBefore: Boolean = false,
        includeSpacesSymbolsAfter: Boolean = false,
    ): String {
        return if (context.start.startIndex < 0 || context.stop.stopIndex < 0)
            context.text
        else {
            val (startToken, endToken) = extendTokensToHiddenTokens(
                context.start,
                context.stop,
                tokenStream,
                includeSpacesSymbolsBefore,
                includeSpacesSymbolsAfter
            )

            context.start.inputStream
                .getText(Interval.of(startToken.startIndex, endToken.stopIndex))
        }
    }

    protected fun extendTokensToHiddenTokens(
        start: Token, stop: Token, tokenStream: TokenStream,
        includeSpacesSymbolsBefore: Boolean = false,
        includeSpacesSymbolsAfter: Boolean = false,
    ): Pair<Token, Token> {
        var startToken = start
        var potentialStartToken = startToken
        // Include hidden parts like comments when copying code up
        while (potentialStartToken.tokenIndex > 0 && tokenStream[potentialStartToken.tokenIndex - 1].channel == Lexer.HIDDEN) {
            val token = tokenStream[potentialStartToken.tokenIndex - 1]
            if (includeSpacesSymbolsBefore || token.text.any { !it.isWhitespace() }) startToken = token
            potentialStartToken = token

        }

        var endToken = stop
        var potentialEndToken = stop

        while (potentialEndToken.tokenIndex < tokenStream.size() - 1 && tokenStream[potentialEndToken.tokenIndex + 1].channel == Lexer.HIDDEN) {
            val token = tokenStream[potentialEndToken.tokenIndex + 1]
            if (includeSpacesSymbolsAfter || token.text.any { !it.isWhitespace() }) endToken = token
            potentialEndToken = token
        }

        return Pair(startToken, endToken)
    }
}
