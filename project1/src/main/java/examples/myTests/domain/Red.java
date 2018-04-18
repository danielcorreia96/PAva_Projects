package examples.myTests.domain;

import ist.meic.pa.GenericFunctions.GenericFunction;

@GenericFunction
public class Red extends Color {
    public static void doThings(Red o) {
        System.out.println("I'm a red thing");
    }

    public static void doThings(Color o) {
        System.out.println("I don't know what to do with this Color. I'll ask...");
        System.out.println(Color.mix(o));
    }

    public static String doString(String s){
        return "Big boy Red handles strings";
    }
}
