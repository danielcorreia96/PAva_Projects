package BasicVersion.CourseTests;

import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;
import BaseTest.BaseTest;

public class TestA extends BaseTest {
    @Test
    public void testA() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestA"});
        String expected = "Red\n" +
                "What color am I?\n" +
                "What color am I?\n";
        Assert.assertEquals(expected, outContent.toString());
    }
}
