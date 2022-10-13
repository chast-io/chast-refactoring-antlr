import java.io.File

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Usage: java -jar RearrangeClassMembers.jar <path_to_file> <path_to_config_file>")
        return
    }

    RearrangeClassMembersRefactoringStrategy.processFile(
        args[0].let { File(it) },
        args[1].let { File(it) },
    ).let {
        println(it.code)
    }
}
