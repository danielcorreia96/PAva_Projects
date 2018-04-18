package examples.myTests.domain;

import ist.meic.pa.GenericFunctions.GenericFunction;

@GenericFunction
interface Rebel {
    public static void doSomething(Object o){
        System.out.println("I'm lazy with Objects. Calling Color bro");
        Color.mix(o);
    }

    public static void doSomething(String s){
        System.out.println("I'm lazy with Strings. Calling Color bro");
        Color.mix(s);
    }

    public static void doSomething(Color c){
        System.out.println("I'm lazy with Colors. I'll call Red this time '");
        Red.doThings(c);
    }
}