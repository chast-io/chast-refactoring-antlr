import config_file.RearrangeClassMembersConfigReader
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

internal class RearrangeClassMembersRefactoringStrategyCSharpTest {

    companion object {
        private fun wrapper(inner: String): String {
            return """
                |public class Wrapper
                |{
                |$inner
                |}
            """.trimMargin()
        }

        private val DEFAULT_CONFIG = RearrangeClassMembersConfigReader.readConfig(
            File("src/test/resources/rearrange_class_members/default_config.yaml")
        )
    }


    @Test
    fun sortComplex() {
        val before = """   
            |// Constructor that takes no arguments:
            |private Person()
            |{
            |    Name = "unknown";
            |}
            |
            |// Constructor that takes one argument:
            |public Person(string name)
            |{
            |    Name = name;
            |}
            |
            |// Auto-implemented readonly property:
            |public string Name { get; }
            |
            |// Method that overrides the base class (System.Object) implementation.
            |public override string ToString()
            |{
            |    return Name;
            |}
        """.replaceIndentByMargin("    ")
        val expected = """
            |// Constructor that takes one argument:
            |public Person(string name)
            |{
            |    Name = name;
            |}
            |
            |// Auto-implemented readonly property:
            |public string Name { get; }
            |
            |// Constructor that takes no arguments:
            |private Person()
            |{
            |    Name = "unknown";
            |}
            |
            |// Method that overrides the base class (System.Object) implementation.
            |public override string ToString()
            |{
            |    return Name;
            |}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(
            wrapper(before),
            SupportedLanguage.CSharp,
            DEFAULT_CONFIG
        )
        assertEquals(wrapper(expected), after.code)
    }
}
