import org.junit.jupiter.api.Test
import config_file.RearrangeClassMembersConfigReader
import java.io.File
import kotlin.test.assertEquals

internal class RearrangeClassMembersRefactoringStrategyKotlinTest {

    companion object {
        private fun wrapper(inner: String): String {
            return """
                |public class Wrapper {
                |$inner
                |}
            """.trimMargin()
        }

        private val DEFAULT_CONFIG = RearrangeClassMembersConfigReader.readConfig(
            File("src/test/resources/rearrange_class_members/default_config.yaml")
        )
    }

    @Test
    fun sortMethods() {
        val before = """
            |private fun method1() {}
            |public fun method2() {}
            |protected fun method3() {}
            |fun method4() {}
        """.replaceIndentByMargin("    ")
        val expected = """
            |public fun method2() {}
            |fun method4() {}
            |protected fun method3() {}
            |private fun method1() {}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }


    @Test
    fun sortVariables() {
        val before = """
            |private var a = 1;
            |public var b = 1;
            |var c = 1;
            |protected var d = 1;
        """.replaceIndentByMargin("    ")
        val expected = """
            |public var b = 1;
            |var c = 1;
            |protected var d = 1;
            |private var a = 1;
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortValues() {
        val before = """
            |private val a = 1;
            |public val b = 1;
            |val c = 1;
            |protected val d = 1;
        """.replaceIndentByMargin("    ")
        val expected = """
            |public val b = 1;
            |val c = 1;
            |protected val d = 1;
            |private val a = 1;
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortConstructors() {
        val before = """
            |private constructor()
            |constructor(a: Int)
            |internal constructor(a: Int, b: Int)
            |protected constructor(a: Int, b: Int, c: Int)
        """.replaceIndentByMargin("    ")
        val expected = """
            |constructor(a: Int)
            |protected constructor(a: Int, b: Int, c: Int)
            |internal constructor(a: Int, b: Int)
            |private constructor()
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortDeclarationOrder() {
        val before = """
            |private constructor(a: Int)
            |var a = 1
            |private fun method1() {}
            |protected var b = 2
            |fun method2() {}
            |constructor()
            |protected fun method3() {}
        """.replaceIndentByMargin("    ")
        val expected = """
            |var a = 1
            |protected var b = 2
            |constructor()
            |private constructor(a: Int)
            |fun method2() {}
            |protected fun method3() {}
            |private fun method1() {}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortDeclarationOrderWithNestedClass() {
        val before = """
            |var a = 1
            |private fun method1() {}
            |protected var b = 2
            |fun method2() {}
            |fun method3() {}
            |internal inner class InnerClass {
            |    private fun method4() {}
            |    fun method5() {}
            |}
        """.replaceIndentByMargin("    ")
        val expected = """
            |var a = 1
            |protected var b = 2
            |fun method2() {}
            |fun method3() {}
            |private fun method1() {}
            |internal inner class InnerClass {
            |    fun method5() {}
            |    private fun method4() {}
            |}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortComplex() {
        val before = """   
            |private fun sampleMethod() {}
            |
            |internal inner class SomethingCouldBeInnerType {
            |    private val foo = ""
            |}
            |
            |private val foo = "foo-value"
            |
            |private enum class Days {
            |    Mon, Tue, Wed, Thu, Fri, Sat, Sun
            |}
            |
            |protected var protectedFoo: String? = null
            |
            |init {
            |    run { val a = Days.Mon }
            |}
            |var defaultModifierFoo: String? = null
            |
            |var publicFoo: String? = null
            |
            |fun RearrangeClassMembersRule() {}
            |
            |companion object {
            |    private const val A_STATIC_FINAL_FIELD = ""
            |
            |    fun staticMethod() {}
            |}
        """.replaceIndentByMargin("    ")
        val expected = """
            |var defaultModifierFoo: String? = null
            |
            |var publicFoo: String? = null
            |
            |protected var protectedFoo: String? = null
            |
            |private val foo = "foo-value"
            |
            |init {
            |    run { val a = Days.Mon }
            |}
            |fun RearrangeClassMembersRule() {}
            |
            |private fun sampleMethod() {}
            |
            |internal inner class SomethingCouldBeInnerType {
            |    private val foo = ""
            |}
            |
            |private enum class Days {
            |    Mon, Tue, Wed, Thu, Fri, Sat, Sun
            |}
            |
            |companion object {
            |    private const val A_STATIC_FINAL_FIELD = ""
    
            |    fun staticMethod() {}
            |}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.Kotlin, DEFAULT_CONFIG)
        assertEquals(wrapper(expected), after.code)
    }
}
