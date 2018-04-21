package BasicVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestH extends BaseTest {
    @Test
    public void testH() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestH"});
        String expected = "Hello is a string.\n" +
                "The number1 is an integer.\n" +
                "The number2.0 is a double.\n";
        Assert.assertEquals(expected, outContent.toString());
    }

}
