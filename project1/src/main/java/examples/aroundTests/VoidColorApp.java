package examples.aroundTests;

import ist.meic.pa.GenericFunctions.GenericFunction;
import ist.meic.pa.GenericFunctionsExtended.AroundMethod;
import ist.meic.pa.GenericFunctionsExtended.WithGenericFunction;
import pa.tests.domain.Bar;
import pa.tests.domain.C1;
import pa.tests.domain.Foo;

@GenericFunction
interface VoidColor {
    public static void doThings(C1 c){
        System.out.println("C1");
    }

    @AroundMethod
    public static void doThings(Foo c){
        System.out.println("Foo Start");
        WithGenericFunction.callNextMethod(c);
        System.out.println("Foo End");
    }

    
    @AroundMethod
    public static void doThings(Bar c){
        System.out.println("Bar Start");
        WithGenericFunction.callNextMethod(c);
        System.out.println("Bar End");
    }

    @AroundMethod
    public static void doThings(Object c){
        System.out.println("Object Start");
        WithGenericFunction.callNextMethod(c);
        System.out.println("Object End");
    }
}

public class VoidColorApp {
    public static void main(String... args){
        Object c = new C1();
        VoidColor.doThings(c);
    }
}
