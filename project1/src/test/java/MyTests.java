import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class MyTests {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    public void testImplementsColorApp() throws Throwable {
        WithGenericFunction.main(new String[]{"examples.myTests.ImplementsColorApp"});
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
