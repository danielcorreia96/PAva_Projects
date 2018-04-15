package examples.myTests;

import ist.meic.pa.GenericFunctions.GenericFunction;

@GenericFunction
interface Color {
    public static String mix(Color c1, Color c2) {
        return mix(c2, c1);
    }

    public static String mix(Red c1, Red c2) {
        return "More red";
    }

    public static String mix(Blue c1, Blue c2) {
        return "More blue";
    }

    public static String mix(Yellow c1, Yellow c2) {
        return "More yellow";
    }

    public static String mix(Red c1, Blue c2) {
        return "Magenta";
    }

    public static String mix(Red c1, Yellow c2) {
        return "Orange";
    }

    public static String mix(Blue c1, Yellow c2) {
        return "Green";
    }
}

class Red implements Color {}
class Blue implements Color {}
class Yellow implements Color {}

public class ImplementsColorApp {
    public static void main(String[] args) {
        Color[] colors = new Color[] { new Red(), new Blue(), new Yellow() };
        for (Color c1 : colors) {
            for (Color c2 : colors) {
                System.out.println(Color.mix(c1,c2));
            }
        }
    }
}
