package pa.tests;

import pa.tests.domain.*;

public class TestA {
    public static void main(String[] args) {
        Color[] colors = new Color[] { new Red(), new Blue(), new Black()};
        for(Color c : colors) System.out.println(Color.mix(c));
    }

}
