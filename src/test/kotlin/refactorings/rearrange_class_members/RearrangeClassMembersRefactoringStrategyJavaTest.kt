package refactorings.rearrange_class_members

import org.junit.jupiter.api.Test
import refactroing_base.SupportedLanguage
import kotlin.test.assertEquals

internal class RearrangeClassMembersRefactoringStrategyJavaTest {

    companion object {
        private fun wrapper(inner: String): String {
            return """
                |public class Wrapper {
                |$inner
                |}
            """.trimMargin()
        }
    }

    @Test
    fun sortMethods() {
        val before = """
            |private void method1() {}
            |public void method2() {}
            |protected void method3() {}
            |void method4() {}
        """.replaceIndentByMargin("    ")
        val expected = """
            |public void method2() {}
            |protected void method3() {}
            |void method4() {}
            |private void method1() {}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.JAVA)
        assertEquals(wrapper(expected), after.code)
    }


    @Test
    fun sortVariables() {
        val before = """
            |private int a = 1;
            |public int b = 1;
            |int c = 1;
            |protected int d = 1;
        """.replaceIndentByMargin("    ")
        val expected = """
            |public int b = 1;
            |protected int d = 1;
            |int c = 1;
            |private int a = 1;
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.JAVA)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortConstructors() {
        val before = """
            |private Wrapper() {}
            |public Wrapper(int a) {}
            |Wrapper(int a, int b) {}
            |protected Wrapper(int a, int b, int c) {}
        """.replaceIndentByMargin("    ")
        val expected = """
            |public Wrapper(int a) {}
            |protected Wrapper(int a, int b, int c) {}
            |Wrapper(int a, int b) {}
            |private Wrapper() {}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.JAVA)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortDeclarationOrder() {
        val before = """
            |private Wrapper(int a) {}
            |int a = 1;
            |private void method1() {}
            |protected int b = 2;
            |public void method2() {}
            |public Wrapper() {}
            |protected void method3() {}
        """.replaceIndentByMargin("    ")
        val expected = """
            |protected int b = 2;
            |int a = 1;
            |public Wrapper() {}
            |private Wrapper(int a) {}
            |public void method2() {}
            |protected void method3() {}
            |private void method1() {}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.JAVA)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortDeclarationOrderWithNestedClass() {
        val before = """
            |int a = 1;
            |private void method1() {}
            |protected int b = 2;
            |public void method2() {}
            |public Wrapper() {}
            |public void method3() {}
            |
            |class InnerClass {
            |    private void method4() {}
            |    public void method5() {}
            |}
        """.replaceIndentByMargin("    ")
        val expected = """
            |protected int b = 2;
            |int a = 1;
            |public Wrapper() {}
            |public void method2() {}
            |public void method3() {}
            |private void method1() {}
            |
            |class InnerClass {
            |    public void method5() {}
            |    private void method4() {}
            |}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.JAVA)
        assertEquals(wrapper(expected), after.code)
    }

    @Test
    fun sortComplex() {
        val before = """   
            |;
            |private void sampleMethod() {
            |
            |}
            |
            |private static final String A_STATIC_FINAL_FIELD = "";
            |
            |class SomethingCouldBeInnerType {
            |    public SomethingCouldBeInnerType() {
            |
            |    }
            |
            |    private String foo = "";
            |}
            |
            |private String foo = "foo-value";
            |
            |private enum Days {
            |    Mon, Tue, Wed, Thu, Fri, Sat, Sun,
            |}
            |
            |protected String protectedFoo;
            |
            |{{
            |    var a = Days.Mon;
            |}}
            |
            |;
            |
            |public static void staticMethod() {
            |
            |}
            |
            |String defaultModifierFoo;
            |public String publicFoo;
            |
            |public RearrangeClassMembersRule() {
            |
            |}
        """.replaceIndentByMargin("    ")
        val expected = """
            |public String publicFoo;
            |
            |protected String protectedFoo;
            |
            |String defaultModifierFoo;
            |
            |private static final String A_STATIC_FINAL_FIELD = "";
            |
            |private String foo = "foo-value";
            |
            |{{
            |    var a = Days.Mon;
            |}}
            |
            |public RearrangeClassMembersRule() {
            |
            |}
            |
            |public static void staticMethod() {
            |
            |}
            |
            |private void sampleMethod() {
            |
            |}
            |class SomethingCouldBeInnerType {
            |    private String foo = "";
    
            |    public SomethingCouldBeInnerType() {
            |
            |    }
            |}
            |
            |private enum Days {
            |    Mon, Tue, Wed, Thu, Fri, Sat, Sun,
            |}
        """.replaceIndentByMargin("    ")

        val after = RearrangeClassMembersRefactoringStrategy.processCodeString(wrapper(before), SupportedLanguage.JAVA)
        assertEquals(wrapper(expected), after.code)
    }
}
