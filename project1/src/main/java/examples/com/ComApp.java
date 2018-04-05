package examples.com;

public class ComApp {
    public static void main(String[] args) {
        Object[] objs1 = new Object[] { "Hello", 1, 'A' };
        Object[] objs2 = new Object[] { "World", 2, 'B' };
        for (Object o1 : objs1) {
            for (Object o2 : objs2) {
                System.out.printf("Combine(%s,%s) -> %s%n", o1, o2, Com.bine(o1, o2));
            }
        }
    }
}
