import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RunTests {
    static Map<String, Method> setups = new HashMap<>();
    static LinkedHashMap<String, Collection<Method> > new_tests = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        Class targetclass = Class.forName(args[0]);
        Class superclass = targetclass.getSuperclass();
        while (superclass != Object.class){
//            System.out.printf("processing superclass annotations %s%n", superclass.getName());
            processSetupAnnotations(superclass);
//            System.out.println("tests map before superclass -> " + new_tests);
            processTestAnnotations(superclass);
//            System.out.println("tests map after superclass -> " + new_tests);
            superclass = superclass.getSuperclass();
        }

//        System.out.printf("%n----------------------------%n");
        processSetupAnnotations(targetclass);
        processTestAnnotations(targetclass);

//        System.out.printf("%n----------------------------%n");
////
//        System.out.println("State after processing annotations");
//        new_tests.entrySet().forEach(System.out::println);
        //System.out.println(new_tests);
//
//        System.out.printf("%n----------------------------%n");

        invokeMethodTests();
    }

    private static void invokeMethodTests() {
        final int[] passed = {0};
        final int[] failed = {0};

            new_tests.forEach((s, methods) -> {
                    //System.out.printf("running test methods for %s%n", s);
                try {
                    for (Method method : methods) {
                        if (method == null){
                            throw new RuntimeException("cenas");
                        }
                        else {
                            method.invoke(null);
                        }
                    }
                    passed[0]++;
                    System.out.printf("Test %s OK!%n", s);
                } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
                    failed[0]++;
                    System.out.printf("Test %s failed%n", s);
                }
            });
        System.out.printf("Passed: %d, Failed %d%n", passed[0], failed[0]);
    }

    private static void processSetupAnnotations(Class clazz) {
        for (Method m: clazz.getDeclaredMethods()){
            if (m.isAnnotationPresent(Setup.class)){
                m.setAccessible(true);
                setups.put(m.getAnnotation(Setup.class).value(),m);
            }
        }
    }

    private static void processTestAnnotations(Class clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class) && Modifier.isStatic(m.getModifiers())) {
                //System.out.printf("processing test annotation at method %s%n",m.getName());
                m.setAccessible(true);
                String[] testValue = m.getAnnotation(Test.class).value();

                Collection<Method> methodList = new_tests.get(m.toString());
                if (methodList == null){
                    methodList = new ArrayList<>();
                }

                if (testValue[0].equals("*")) {
                    methodList.addAll(setups.values());
                }
                else {
                    Arrays.stream(testValue).map(s -> setups.get(s)).forEach(methodList::add);
                }
                methodList.add(m);
                new_tests.put(m.toString(),methodList);
            }
        }
    }
}