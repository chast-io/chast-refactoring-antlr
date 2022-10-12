package refactroing_base

import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.misc.Interval

object TokenRangeUtils {
    /**
     * https://stackoverflow.com/a/58719524/7893818
     * Explanation: Get the first token, get the last token, and get the text from the input stream between the first char of the first token and the last char of the last token.
     */
    fun getFullText(
        context: ParserRuleContext, tokenStream: TokenStream,
        config: TokenExtensionConfig = TokenExtensionConfig()
    ): String {
        return if (context.start.startIndex < 0 || context.stop.stopIndex < 0)
            context.text
        else {
            val (startToken, endToken) = extendTokensToHiddenTokens(
                context.start,
                context.stop,
                tokenStream,
                config
            )

            context.start.inputStream
                .getText(Interval.of(startToken.startIndex, endToken.stopIndex))
        }
    }

    fun extendTokensToHiddenTokens(
        start: Token, stop: Token, tokenStream: TokenStream,
        config: TokenExtensionConfig = TokenExtensionConfig()
    ): Pair<Token, Token> {
        var startToken = start
        var potentialStartToken = startToken
        // Include hidden parts like comments when copying code up
        while (potentialStartToken.tokenIndex > 0 && tokenStream[potentialStartToken.tokenIndex - 1].channel in config.channelsToIncludeBefore) {
            val token = tokenStream[potentialStartToken.tokenIndex - 1]
            if (token.text.isNotBlank() || config.includeWhitespaceSymbolsBefore) startToken = token
            potentialStartToken = token

        }

        var endToken = stop
        var potentialEndToken = stop

        while (potentialEndToken.tokenIndex < tokenStream.size() - 1 && tokenStream[potentialEndToken.tokenIndex + 1].channel in config.channelsToIncludeAfter) {
            val token = tokenStream[potentialEndToken.tokenIndex + 1]
            if (token.text.isNotBlank() || config.includeWhitespaceSymbolsAfter) endToken = token
            potentialEndToken = token
        }

        return Pair(startToken, endToken)
    }


    public data class TokenExtensionConfig(
        val channelsToIncludeBefore: List<Int> = listOf(Lexer.HIDDEN),
        val channelsToIncludeAfter: List<Int> = listOf(Lexer.HIDDEN),
        val includeWhitespaceSymbolsBefore: Boolean = false,
        val includeWhitespaceSymbolsAfter: Boolean = false,
    )
}
