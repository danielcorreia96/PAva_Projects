package examples.aroundTests;

import ist.meic.pa.GenericFunctions.GenericFunction;
import ist.meic.pa.GenericFunctionsExtended.AroundMethod;
import ist.meic.pa.GenericFunctionsExtended.WithGenericFunction;
import pa.tests.domain.Bar;
import pa.tests.domain.C1;
import pa.tests.domain.Foo;

@GenericFunction
interface StringColor {
    public static String doThings(C1 c){
        return "C1";
    }

    @AroundMethod
    public static String doThings(Foo c){
        return "FooStart##" + WithGenericFunction.callNextMethod(c) + "##FooEnd";
    }

    @AroundMethod
    public static String doThings(Bar c){
        return "BarStart>>" + WithGenericFunction.callNextMethod(c) + "<<BarEnd";
    }

    @AroundMethod
    public static String doThings(Object c){
        return "ObjectStart__" + WithGenericFunction.callNextMethod(c) + "__ObjectEnd";
    }
}

public class StringColorApp {
    public static void main(String... args){
        Object c = new C1();
        System.out.println(StringColor.doThings(c));
    }
}
