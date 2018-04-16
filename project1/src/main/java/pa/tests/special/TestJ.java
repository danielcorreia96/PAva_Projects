package pa.tests.special;

import pa.tests.domain.Blue;
import pa.tests.domain.Color;
import pa.tests.domain.What;

public class TestJ {
    public static void main(String[] args) {
        Color blue = new Blue();
        What.is(blue);
    }
}
