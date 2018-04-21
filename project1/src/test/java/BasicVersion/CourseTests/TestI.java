package BasicVersion.CourseTests;

import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;
import BaseTest.BaseTest;
public class TestI extends BaseTest {


    @Test
    public void testI() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestI"});
        String expected = "What is black?  Is it an object? Is it a color? It is all of that and much more...What is red?  Is it an object? Is it a color?";
        Assert.assertEquals(expected, outContent.toString());
    }

}
