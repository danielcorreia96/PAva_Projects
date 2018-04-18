package examples.myTests.domain;

public class SubMakeIt extends MakeIt {
    public void doThings(Object o){
        System.out.println("Who are you? Calling MakeIt now...");
        MakeIt.ddouble(o);
    }