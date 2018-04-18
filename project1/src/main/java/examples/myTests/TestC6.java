package examples.myTests;

import examples.myTests.domain.C1;
import examples.myTests.domain.SubMakeIt;

public class TestC6 {
    public static void main(String[] args) {
        C1 c1 = new C1();
        SubMakeIt sbm = new SubMakeIt();
        sbm.doThings(c1);
    }
}