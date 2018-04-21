package ExtendedVersion.CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestM extends BaseTest {


    @Test
    public void testM() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"pa.tests.TestM"});
        String expected = "More red\n" +
                "Magenta\n" +
                "Orange\n" +
                "Magenta\n" +
                "More blue\n" +
                "Green\n" +
                "Orange\n" +
                "Green\n" +
                "More yellow\n";
        assertEquals(expected, outContent.toString());
    }
}
