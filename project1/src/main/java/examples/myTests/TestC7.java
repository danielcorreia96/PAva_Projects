package examples.myTests;

import examples.myTests.domain.C1;
import examples.myTests.domain.SuperMakeIt;

public class TestC7 {
    public static void main(String[] args) {
        C1 c1 = new C1();
        SuperMakeIt spm = new SuperMakeIt();
        spm.doThings(c1);
    }
}