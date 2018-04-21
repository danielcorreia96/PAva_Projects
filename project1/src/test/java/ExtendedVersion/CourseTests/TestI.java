package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestI extends BaseTest {


    @Test
    public void testI() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestI"});
        String expected = "What is black?  Is it an object? Is it a color? It is all of that and much more...What is red?  Is it an object? Is it a color?";
        Assert.assertEquals(expected, outContent.toString());
    }

}
