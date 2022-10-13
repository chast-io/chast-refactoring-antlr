import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: java -jar RemoveDoubleNegation.jar <path_to_file>")
        return
    }

    RemoveDoubleNegationRefactoring.processFile(
        File(args[0]),
    ).let {
        println(it.code)
    }
}
