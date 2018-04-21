package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestN extends BaseTest {

    @Test
    public void testN() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestN"});
        String expected = "Red-Color-Red\n" +
                "SuperBlack-Black-Color\n" +
                "SuperBlack-Black-SuperBlack\n";
        assertEquals(expected, outContent.toString());
    }
}
