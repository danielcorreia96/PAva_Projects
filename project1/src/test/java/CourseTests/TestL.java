package CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestL extends BaseTest {

    @Test
    public void testL() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestL"});
        String expected = "Combine(Hello, World) -> Hello, World!\n" +
                "Combine(Hello, 2) -> HelloHello\n" +
                "Combine(Hello, B) -> Hello, B!\n" +
                "Combine(1, World) -> [1, World]\n" +
                "Combine(1, 2) -> 3\n" +
                "Combine(1, B) -> [1, B]\n" +
                "Combine(A, World) -> [A, World]\n" +
                "Combine(A, 2) -> [A, 2]\n" +
                "Combine(A, B) -> [A, B]\n";
        Assert.assertEquals(expected, outContent.toString());
    }
}
