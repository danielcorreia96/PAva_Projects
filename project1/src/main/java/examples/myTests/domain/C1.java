package examples.myTests.domain;

public class C1 implements Foo, Bar {
    public static void doThings(C1 c1) {
        System.out.println("C1 can't handle C1 instances. Calling MakeIt...");
        MakeIt.ddouble(c1);
    }
}
