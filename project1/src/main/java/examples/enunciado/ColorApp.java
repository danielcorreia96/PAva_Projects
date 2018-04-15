package examples.enunciado;

import meic.ist.pa.GenericFunctions.GenericFunction;

@GenericFunction
class Color {
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

class Red extends Color {}
class Blue extends Color {}
class Yellow extends Color {}

public class ColorApp {
    public static void main(String[] args) {
        Color[] colors = new Color[] { new Red(), new Blue(), new Yellow() };
        for (Color c1 : colors) {
            for (Color c2 : colors) {
                System.out.println(Color.mix(c1,c2));
            }
        }
    }
}
