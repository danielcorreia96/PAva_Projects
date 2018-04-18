package examples.myTests;

import examples.myTests.domain.Red;
import examples.myTests.domain.Rebel;

public class TestC4 {
    public static void main(String[] args) {
        Object[] stuff = new Object[] { "Hello", new Red(), 2.0 };
        for (Object obj : stuff) {
            Rebel.doSomething(obj);
        }
    }
}