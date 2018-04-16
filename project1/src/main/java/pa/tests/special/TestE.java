package pa.tests.special;

import pa.tests.domain.*;


public class TestE {
    public static void main(String[] args) {
        Object objects = new Object[] { 123, "Foo", 1.2};
        System.out.println(Identify.it(objects));
    }
}
