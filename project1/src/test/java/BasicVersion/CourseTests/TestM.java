package BasicVersion.CourseTests;

import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;
import BaseTest.BaseTest;
import static org.junit.Assert.assertEquals;

public class TestM extends BaseTest {


    @Test
    public void testM() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestM"});
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
