package examples.aroundTests;

import ist.meic.pa.GenericFunctions.GenericFunction;
import ist.meic.pa.GenericFunctionsExtended.AroundMethod;
import ist.meic.pa.GenericFunctionsExtended.WithGenericFunction;
import pa.tests.domain.Bar;
import pa.tests.domain.C1;
import pa.tests.domain.Foo;

@GenericFunction
interface Color {
    public static void doThings(C1 c){
        System.out.println("C1");
    }

    @AroundMethod
    public static void doThings(Foo c){
        System.out.println("Foo");
        WithGenericFunction.callNextMethod(c);
        System.out.println("Foo");
    }

    
    @AroundMethod
    public static void doThings(Bar c){
        System.out.println("Bar");
        WithGenericFunction.callNextMethod(c);
        System.out.println("Bar");
    }

    @AroundMethod
    public static void doThings(Object c){
        System.out.println("Object");
        WithGenericFunction.callNextMethod(c);
        System.out.println("Object");
    }
}

public class ColorApp {
    public static void main(String... args){
        Object c = new C1();
        Color.doThings(c);

        System.out.println();

        Color.doThings(c);
    }
}
