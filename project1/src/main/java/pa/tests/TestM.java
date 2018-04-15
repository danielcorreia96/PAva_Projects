package pa.tests;

import pa.tests.domain.*;


public class TestM {
    public static void main(String[] args) {
        Color[] colors = new Color[]{new Red(), new Blue(), new Yellow()};
        for (Color c1 : colors)
            for (Color c2 : colors)
                System.out.println(Color.mix(c1, c2));
    }
}
