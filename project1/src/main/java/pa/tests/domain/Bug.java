package pa.tests.domain;

import ist.meic.pa.GenericFunctions.GenericFunction;

@GenericFunction
public class Bug {
    // Test F
    public static void bug(Object o) {
        System.out.println("Object");
    }
    public static void bug(Foo f){
        System.out.println("Foo");
    }
    public static void bug(Bar b){ System.out.println("Bar"); }
}
