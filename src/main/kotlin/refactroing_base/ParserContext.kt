package refactroing_base

import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter
import org.antlr.v4.runtime.tree.ParseTree

data class ParserContext(
    val lexer: Lexer,
    val parser: Parser,
    val tokenStream: CommonTokenStream,
    val rewriter: TokenStreamRewriter,
    val parseTreeRoot: ParseTree,
    val language: SupportedLanguage
)
