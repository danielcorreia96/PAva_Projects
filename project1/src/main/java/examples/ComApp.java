package examples;


import meic.ist.pa.GenericFunctions.GenericFunction;

import java.util.Vector;

@GenericFunction
interface Com {
    public static Object bine(Object a, Object b) {
        Vector<Object> v = new Vector<Object>();
        v.add(a);
        v.add(b);
        return v;
    }

    public static Integer bine(Integer a, Integer b) {
        return a + b;
    }

    public static Object bine(String a, Object b) {
        return a + ", " + b + "!";
    }

    public static Object bine(String a, Integer b) {
        return (b == 0) ? "" : a + bine(a, b - 1);

    }
}

public class ComApp {
    public static void main(String[] args) {
        Object[] objs1 = new Object[] { "Hello", 1, 'A' };
        Object[] objs2 = new Object[] { "World", 2, 'B' };
        for (Object o1 : objs1) {
            for (Object o2 : objs2) {
                System.out.printf("Combine(%s,%s) -> %s%n", o1, o2, Com.bine(o1, o2));
            }
        }
    }
}
