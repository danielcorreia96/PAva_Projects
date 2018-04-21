package ExtendedVersion.ExamplesTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestColorApp extends BaseTest{
    @Test public void testColorApp() throws Throwable {
        ist.meic.pa.GenericFunctionsExtended.WithGenericFunction.main(new String[]{"examples.enunciado.ColorApp"});
        String expected =
                "More red\n" +
                        "Magenta\n" +
                        "Orange\n" +
                        "Magenta\n" +
                        "More blue\n" +
                        "Green\n" +
                        "Orange\n" +
                        "Green\n" +
                        "More yellow\n";
        assertEquals("Wrong output",expected, outContent.toString());
    }
}
