import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RemoveDoubleNegationRefactoringPythonTest {

    companion object {
        private fun wrapper(inner: String): String {
            return inner
        }
    }

    @Test
    fun doubleSimpleBoolean1() {
        val before = "print(not not True)"
        val expected = "print(True)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleBoolean2() {
        val before = "print(not not False)"
        val expected = "print(False)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleDropSpacesBefore() {
        val before = "print(not not  False)"
        val expected = "print(False)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleKeepEnclosingBracesFromSimpleStatement() {
        val before = "print(not not (False))"
        val expected = "print((False))"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleSimpleRemoveEnclosingBracesFromSimpleStatement() {
        val before = "print(not (not False))"
        val expected = "print(False)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun tripleSimple() {
        val before = "print(not not not True)"
        val expected = "print(not True)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun quadrupleSimple() {
        val before = "print(not not not not True)"
        val expected = "print(True)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun quadrupleWithParentheses() {
        val before = "print(not (((not not not True))))"
        val expected = "print(True)"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun twoNegationsNotReplaceableAND() {
        val before = "print(not (not True && False))"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(expected, after.changed)
    }


    @Test
    fun twoNegationsNotReplaceableOR() {
        val before = "print(not (not True || False))"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(expected, after.changed)
    }


    @Test
    fun doubleInIf() {
        val before = """
            |if (not (not False)):
            |    return "Hello"
        """.replaceIndentByMargin("")
        val expected = """
            |if (False):
            |    return "Hello"
        """.replaceIndentByMargin("")


        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleInIfNoParentheses() {
        val before = """
            |if not (not False):
            |    return "Hello"
        """.replaceIndentByMargin("")
        val expected = """
            |if False:
            |    return "Hello"
        """.replaceIndentByMargin("")


        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun doubleInFunctionCall() {
        val before = "print(not (not testWhatever(12)))"
        val expected = "print(testWhatever(12))"

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun ignoreLineComments() {
        val before = "# print(not (not not  True || False))"
        val expected = false

        val after = RemoveDoubleNegationRefactoring.processCodeString(wrapper(before), SupportedLanguage.PYTHON)
        assertEquals(expected, after.changed)
    }

}
