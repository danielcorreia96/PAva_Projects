package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestB extends BaseTest {

    @Test
    public void testB() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestB"});
        String expected = "Object\n" +
                "Foo\n" +
                "123\n";
        Assert.assertEquals(expected, outContent.toString());
    }

}
