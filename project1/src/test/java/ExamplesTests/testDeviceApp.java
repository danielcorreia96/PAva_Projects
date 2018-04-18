package ExamplesTests;/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import  BaseTest.BaseTest;
import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class testDeviceApp extends BaseTest{

    @Test public void testDeviceApp() throws Throwable {
        WithGenericFunction.main(new String[]{"examples.enunciado.DeviceApp"});
        String expected =
                "draw a line on screen!\n" +
                "draw a circle on screen!\n" +
                "draw a line on printer!\n" +
                "draw a circle on printer!\n";
        assertEquals("Wrong output",expected, outContent.toString());
    }
}
