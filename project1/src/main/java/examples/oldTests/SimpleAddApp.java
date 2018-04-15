package examples.oldTests;

import ist.meic.pa.GenericFunctions.GenericFunction;

import java.util.Arrays;

@GenericFunction
interface SimpleAdd {

    public static Object add(Object[] a, Object[] b){
        Object[] r = new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = add(a[i], b[i]);
        }
        return r;
    }

    public static Object add(Object o, Object o1) {
        return "No method implementation";
    }

    public static Object add(Integer a, Integer b){
        return a + b;
    }
}

public class SimpleAddApp {
    public static void main(String[] args) {
        println(SimpleAdd.add(1, 3));
        println(SimpleAdd.add(new Object[] { 1, 2, 3 }, new Object[] { 4, 5, 6 }));
        println(SimpleAdd.add(new Object[] { 1, 2 }, 3));
    }

    public static void println(Object obj) {

        if (obj instanceof Object[]) {
            System.err.println(Arrays.deepToString((Object[])obj));
        } else {
            System.err.println(obj);
        }
    }

}
