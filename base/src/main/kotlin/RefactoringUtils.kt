import org.antlr.v4.runtime.tree.ParseTree
import java.io.File

object RefactoringUtils {

    fun readFile(file: File): String {
        if (!file.exists()) throw IllegalArgumentException("File does not exist at '${file.absolutePath}'")
        return file.readText()
    }

    fun <T : ParseTree> walkAndProcess(
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

    fun <T : ParseTree> walkAndProcessOncePerPath(
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
}
