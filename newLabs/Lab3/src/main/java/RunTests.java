import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RunTests {
    private static Map<String, Method> setups = new HashMap<>();
    private static LinkedHashMap<String, Collection<Method> > new_tests = new LinkedHashMap<>();
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        Class target = Class.forName(args[0]);
        Class superclass = target.getSuperclass();

        // Process annotations in target class hierarchy
        while (superclass != Object.class){
            processSetupAnnotations(superclass);
            processTestAnnotations(superclass);
            superclass = superclass.getSuperclass();
        }

        // Process annotations for target class
        processSetupAnnotations(target);
        processTestAnnotations(target);

        // Run tests
        new_tests.forEach(RunTests::invokeMethod);
        System.out.printf("Passed: %d, Failed %d%n", passed, failed);
    }
    private static void invokeMethod(String testName, Collection<Method> methods){
        try {
            for (Method method : methods) {
                if (method == null) throw new RuntimeException();
                else method.invoke(null);
            }
            passed++;
            System.out.printf("Test %s OK!%n", testName);
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            failed++;
            System.out.printf("Test %s failed%n", testName);
        }
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
                m.setAccessible(true);
                String[] testValue = m.getAnnotation(Test.class).value();

                Collection<Method> methodList = Optional.ofNullable(new_tests.get(m.toString())).orElse(new ArrayList<>());

                if (testValue[0].equals("*")) methodList.addAll(setups.values());
                else Arrays.stream(testValue).map(s -> setups.get(s)).forEach(methodList::add);

                methodList.add(m);
                new_tests.put(m.toString(),methodList);
            }
        }
    }
}