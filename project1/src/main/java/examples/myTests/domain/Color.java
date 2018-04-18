package examples.myTests.domain;

import ist.meic.pa.GenericFunctions.GenericFunction;

@GenericFunction
public class Color {

    public static String mix(Object o) {
        return "I'm just an object.";
    }

    public static String mix(Color c1) {
        return "What color am I?";
    }

    public static String mix(Red c1) {
        return "Red";
    }

    public static String mix(Object[] arr) {
        String res = "";
        for (Object o : arr)
            res += mix(o);
        return res;
    }

    public static String mix(String s){
        System.out.println("I don't know how to handle String. I'll ask Red...");
        return Red.doString(s);
    }
    
}