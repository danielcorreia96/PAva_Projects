package CourseTests;

import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestN extends BaseTest {

    @Test
    public void testN() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestN"});
        String expected = "Red-Color-Red\n" +
                "SuperBlack-Black-Color\n" +
                "SuperBlack-Black-SuperBlack\n";
        assertEquals(expected, outContent.toString());
    }
}
