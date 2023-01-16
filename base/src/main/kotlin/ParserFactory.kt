import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.TokenStreamRewriter

object ParserFactory {
    public fun getParseTreeForExtension(extension: String, code: String): ParserContext =
        getParseTreeForLanguage(SupportedLanguage.getLanguageFromExtension(extension), code)

    public fun getParseTreeForLanguage(languages: SupportedLanguage, code: String): ParserContext {
        return when (languages) {
            SupportedLanguage.Python -> getPythonParserContextForCode(code)
            SupportedLanguage.Java -> getJavaParserContextForCode(code)
            SupportedLanguage.CSharp -> getCSharpParserContextForCode(code)
            SupportedLanguage.Kotlin -> getKotlinParserContextForCode(code)
        }
    }

    public fun getParseTreeForLanguage(extension: String): (String) -> ParserContext =
        getParserForLanguage(SupportedLanguage.getLanguageFromExtension(extension))


    public fun getParserForLanguage(languages: SupportedLanguage): (String) -> ParserContext {
        return when (languages) {
            SupportedLanguage.Python -> ParserFactory::getPythonParserContextForCode
            SupportedLanguage.Java -> ParserFactory::getJavaParserContextForCode
            SupportedLanguage.CSharp -> ParserFactory::getCSharpParserContextForCode
            SupportedLanguage.Kotlin -> ParserFactory::getKotlinParserContextForCode
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
            language = SupportedLanguage.Python
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
            language = SupportedLanguage.Java
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

    private fun getKotlinParserContextForCode(code: String): ParserContext {
        val lexer = KotlinLexer(CharStreams.fromString(code))
        val tokenStream = CommonTokenStream(lexer)
        val rewriter = TokenStreamRewriter(tokenStream)
        val parser = KotlinParser(tokenStream)

        return ParserContext(
            lexer = lexer,
            parser = parser,
            tokenStream = tokenStream,
            rewriter = rewriter,
            parseTreeRoot = parser.kotlinFile(),
            language = SupportedLanguage.Kotlin
        )
    }
}
