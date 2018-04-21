package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestD extends BaseTest {

    @Test
    public void testD() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestD"});
        String expected = "Foo111\n" +
                "123477\n";
        Assert.assertEquals(expected, outContent.toString());
    }

}
