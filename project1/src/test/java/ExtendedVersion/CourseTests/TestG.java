package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;

public class TestG extends BaseTest {
    @Test
    public void testG() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestG"});
        String expected = "4\n" +
                "[5, 7, 9]\n" +
                "[[4, 6], 8]\n";
        Assert.assertEquals(expected, outContent.toString());
    }
}
