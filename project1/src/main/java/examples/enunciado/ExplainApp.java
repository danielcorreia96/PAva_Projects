package examples.enunciado;

import meic.ist.pa.GenericFunctions.GenericFunction;
import meic.ist.pa.GenericFunctions.BeforeMethod;
import meic.ist.pa.GenericFunctions.AfterMethod;

@GenericFunction
interface Explain {
    public static void it(Integer i) {
        System.out.print(i + " is an integer");
    }

    public static void it(Double i) {
        System.out.print(i + " is a double");
    }

    public static void it(String s) {
        System.out.print(s + " is a string");
    }

    @BeforeMethod
    public static void it(Number n) {
        System.out.print("The number ");
    }

    @AfterMethod
    public static void it(Object o) {
        System.out.println(".");
    }
}

public class ExplainApp {

    public static void main(String[] args) {
        Object[] objs = new Object[] { "Hello", 1, 2.0 };
        for (Object o : objs) {
            Explain.it(o);
        }
    }
}
