import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RemoveDoubleNegationRefactoringJavaTest {

    companion object {
        private fun wrapper(inner: String): String {
            return """
                |public class Wrapper {
                |    public static void main(String[] args) {
                |        $inner
                |    }
                |}
            """.trimMargin()
        }
    }

    @Test
    fun doubleSimpleBoolean1() {
        val before = "print(!!true);"
        val expected = "print(true);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleBoolean2() {
        val before = "print(!!false);"
        val expected = "print(false);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleDropSpacesBefore1() {
        val before = "print(!! false );"
        val expected = "print(false );"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleDropSpacesBefore2() {
        val before = "print(!! /*COMMENT*/ false );"
        val expected = "print(/*COMMENT*/ false );"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleKeepEnclosingBracesFromSimpleStatement() {
        val before = "print(!!(false));"
        val expected = "print((false));"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleRemoveEnclosingBracesFromSimpleStatement() {
        val before = "print(!(!false));"
        val expected = "print(false);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun tripleSimple() {
        val before = "print(!!!true);"
        val expected = "print(!true);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun quadrupleSimple() {
        val before = "print(!!!!true);"
        val expected = "print(true);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun quadrupleWithParentheses() {
        val before = "print(!(((!!!true))));"
        val expected = "print(true);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun twoNegationsNotReplaceableAND() {
        val before = "print(!(!true && false));"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(expected, after.changed)
    }


    @Test
    fun twoNegationsNotReplaceableOR() {
        val before = "print(!(!true || false));"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(expected, after.changed)
    }

    @Test
    fun doubleWithComments() {
        val before = "print(/*comment1*/!/*comment2*/(!/*comment3*/true/*comment4*/)/*comment5*/);"
        val expected = "print(/*comment1*//*comment3*/true/*comment4*//*comment5*/);"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleInIf() {
        val before = "if (!(!false)) return \"Hello\";"
        val expected = "if (false) return \"Hello\";"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleInFunctionCall() {
        val before = "print(!(!testWhatever(12)));"
        val expected = "print(testWhatever(12));"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun ignoreLineComments() {
        val before = "//print(!(!! true || false))"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(expected, after.changed)
    }

    @Test
    fun ignoreBlockComments() {
        val before = "/*print(!(!! true || false))*/"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.Java)
        assertEquals(expected, after.changed)
    }
}
