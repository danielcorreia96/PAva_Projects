package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestK extends BaseTest {

    @Test
    public void testK() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestK"});
        String expected = "List, List\n" +
                "[[[Hello, 2], [Hello, B]], [3, [1, B]], [[A, 2], [A, B]]]\n";
        Assert.assertEquals(expected, outContent.toString());
    }

}
