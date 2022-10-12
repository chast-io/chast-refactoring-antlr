package refactroing_base

import CSharpLexer
import CSharpParser
import JavaLexer
import JavaParser
import PythonLexer
import PythonParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

object ParserFactory {
    public fun getParseTreeForExtension(extension: String, code: String): ParserContext =
        getParseTreeForLanguage(SupportedLanguage.getLanguageFromExtension(extension), code)

    public fun getParseTreeForLanguage(languages: SupportedLanguage, code: String): ParserContext {
        return when (languages) {
            SupportedLanguage.PYTHON -> getPythonParserContextForCode(code)
            SupportedLanguage.JAVA -> getJavaParserContextForCode(code)
            SupportedLanguage.CSharp -> getCSharpParserContextForCode(code)
        }
    }

    public fun getParseTreeForLanguage(extension: String): (String) -> ParserContext =
        getParserForLanguage(SupportedLanguage.getLanguageFromExtension(extension))


    public fun getParserForLanguage(languages: SupportedLanguage): (String) -> ParserContext {
        return when (languages) {
            SupportedLanguage.PYTHON -> ::getPythonParserContextForCode
            SupportedLanguage.JAVA -> ::getJavaParserContextForCode
            SupportedLanguage.CSharp -> ::getCSharpParserContextForCode
        }
    }


    private fun getPythonParserContextForCode(code: String): ParserContext {
        val lexer = PythonLexer(CharStreams.fromString(code))
        val tokenStream = CommonTokenStream(lexer)
        val rewriter = TokenStreamRewriter(tokenStream)
        val parser = PythonParser(tokenStream)

        return ParserContext(
            lexer = lexer,
            parser = parser,
            tokenStream = tokenStream,
            rewriter = rewriter,
            parseTreeRoot = parser.root(),
            language = SupportedLanguage.PYTHON
        )
    }


    private fun getJavaParserContextForCode(code: String): ParserContext {
        val lexer = JavaLexer(CharStreams.fromString(code))
        val tokenStream = CommonTokenStream(lexer)
        val rewriter = TokenStreamRewriter(tokenStream)
        val parser = JavaParser(tokenStream)

        return ParserContext(
            lexer = lexer,
            parser = parser,
            tokenStream = tokenStream,
            rewriter = rewriter,
            parseTreeRoot = parser.compilationUnit(),
            language = SupportedLanguage.JAVA
        )
    }

    private fun getCSharpParserContextForCode(code: String): ParserContext {
        val lexer = CSharpLexer(CharStreams.fromString(code))
        val tokenStream = CommonTokenStream(lexer)
        val rewriter = TokenStreamRewriter(tokenStream)
        val parser = CSharpParser(tokenStream)

        return ParserContext(
            lexer = lexer,
            parser = parser,
            tokenStream = tokenStream,
            rewriter = rewriter,
            parseTreeRoot = parser.compilation_unit(),
            language = SupportedLanguage.CSharp
        )
    }

}
