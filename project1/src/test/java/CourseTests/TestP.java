package CourseTests;

import BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestP extends BaseTest {

    @Test
    public void testP() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestP"});
        String expected = "Let me be the first!\n" +
                "How come Integer-Integer is more specific than me?\n" +
                "Woho!! I'm the primary!\n" +
                "Muahaha! I knew I would run after the primary!\n" +
                "Sniff, Sniff! Why am I the last? I'm more specific than Obj-Obj!\n";
        assertEquals(expected, outContent.toString());
    }
}
