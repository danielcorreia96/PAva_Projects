package BasicVersion.ExamplesTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestComApp extends BaseTest{
    @Test public void testComApp() throws Throwable {
        WithGenericFunction.main(new String[]{"examples.enunciado.ComApp"});
        String expected =
                "Combine(Hello,World) -> Hello, World!\n" +
                        "Combine(Hello,2) -> HelloHello\n" +
                        "Combine(Hello,B) -> Hello, B!\n" +
                        "Combine(1,World) -> [1, World]\n" +
                        "Combine(1,2) -> 3\n" +
                        "Combine(1,B) -> [1, B]\n" +
                        "Combine(A,World) -> [A, World]\n" +
                        "Combine(A,2) -> [A, 2]\n" +
                        "Combine(A,B) -> [A, B]\n";
        assertEquals("Wrong output",expected, outContent.toString());
    }
}
