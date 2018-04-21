package ExtendedVersion.ExamplesTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestExplainApp  extends BaseTest{
    @Test
    public void testExplainApp() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"examples.enunciado.ExplainApp"});
        String expected =
                "Hello is a string.\n" +
                        "The number 1 is an integer.\n" +
                        "The number 2.0 is a double.\n";
        assertEquals("Wrong output",expected, outContent.toString());
    }
}
