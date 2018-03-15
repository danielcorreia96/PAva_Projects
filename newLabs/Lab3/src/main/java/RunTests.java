import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class RunTests {
    static Map<String, Method> setups = new HashMap<>();

    public static void main(String[] args) throws Exception {


        Class superclass = Class.forName(args[0]).getSuperclass();
        while (!(superclass.getSimpleName().equals("Object"))){
            processSetupAnnotations(superclass);
            processTestAnnotations(superclass);
            superclass = superclass.getSuperclass();
        }

        Class targetclass = Class.forName(args[0]);
        processSetupAnnotations(targetclass);
        processTestAnnotations(targetclass);
    }

    private static void processSetupAnnotations(Class clazz) {
        for (Method m: clazz.getDeclaredMethods()){
            if (m.isAnnotationPresent(Setup.class)){
                //System.out.println("process Setup annotation");
                m.setAccessible(true);
                setups.put(m.getAnnotation(Setup.class).value(),m);
            }
        }
    }

    private static void processTestAnnotations(Class clazz) {
        int passed = 0, failed = 0;

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class) && Modifier.isStatic(m.getModifiers())) {
                try {
                    m.setAccessible(true);
                    // check value
                    String[] testValue = m.getAnnotation(Test.class).value();
                    //System.out.println(testValue[0]);
                    if (testValue[0].equals("*")){
                        setups.forEach((s, method) -> {
                            try {
                                method.invoke(null);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException();
                            }
                        });
                    }
                    else {
                        for (String s : testValue) {
                            setups.get(s).invoke(null);
                        }
                    }

                    m.invoke(null);
                    passed++;
                    System.out.printf("Test %s OK!%n", m);
                } catch (Throwable ex) {
                    System.out.printf("Test %s failed%n", m);
                    failed++;
                }
            }
        }
        System.out.printf("Passed: %d, Failed %d%n", passed, failed);
    }
}