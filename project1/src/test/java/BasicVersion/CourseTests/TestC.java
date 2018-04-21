package BasicVersion.CourseTests;

import ist.meic.pa.GenericFunctions.WithGenericFunction;
import org.junit.Assert;
import org.junit.Test;
import BaseTest.BaseTest;

public class TestC extends BaseTest {
    @Test
    public void testC() throws Throwable {
        WithGenericFunction.main(new String[]{"pa.tests.TestC"});
        String expected = "RedI'm just an object.What color am I?I'm just an object.\n";
        Assert.assertEquals(expected, outContent.toString());
    }
}
