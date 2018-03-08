package pt.ist.ap.labs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.out;

public class Shell {
    private static Object previous = null;
    private static Scanner sc = new Scanner(System.in);
    private static Map<String,Object> objectMap = new HashMap<>();

    private static final Map<String, Class<?>> PRIMITIVES_MAP = new HashMap<>(){{
        put("int", Integer.class);
        put("double", Double.class);
        put("float", Float.class);
        put("char", Character.class);
        put("boolean", Boolean.class);
        put("byte", Byte.class);
        put("long", Long.class);
        put("short", Short.class);
        put("void", Void.class);
    }};

    private static final Map<String, Class<?>> PRIMITIVES_CLASSES = new HashMap<>(){{
        put("int", int.class);
        put("double", double.class);
        put("float", float.class);
        put("char", char.class);
        put("boolean", boolean.class);
        put("byte", byte.class);
        put("long", long.class);
        put("short", short.class);
        put("void", void.class);
    }};

    public static void main(String... args){
        final Map<String, Runnable> commands = new HashMap<>(){{
            put("Class", Shell::getClassByName);
            put("Set", Shell::setVariable);
            put("Get", () -> previous = objectMap.get(sc.next()));
            put("Index", () -> previous = ((Object[]) previous)[Integer.parseInt(sc.next())]);
            put("Package", Shell::printPackageInformation);
            put("Exit", () -> { out.println("Goodbye!"); System.exit(1); } );
        }};

        while (true) {
            out.print("Command:> ");
            String command = sc.next();
            try {
                if (commands.get(command) == null)
                    callGenericCommand(command);
                else
                    commands.get(command).run();
            }
            catch (Exception e) {
                out.printf("SOME ERROR: %s",e.getMessage());
            }
            printPreviousResult();
        }
    }

    private static void setVariable() {
        String key = sc.next();
        objectMap.put(key, previous);
        previous = objectMap.get(key);
        out.printf("Saved name for object of type %s%n", previous.getClass());
    }

    private static void getClassByName() {
        try {
            previous = Class.forName(sc.next());
        } catch (ClassNotFoundException e) {
            out.println("Unable to find class");
        }
    }

    private static void printPreviousResult() {
        if (previous.getClass().isArray()){
            Arrays.stream((Object[]) previous).forEach(out::println);
        }
        else {
            out.println(previous.toString());
        }
    }

    private static void printPackageInformation() {
        final Package aPackage = previous.getClass().getPackage();
        final String package_info = sc.nextLine().trim();
        final List<String> information_types = Arrays.asList("Implementation", "Annotation", "Specification", "Name");

        out.printf("Package information (of [%s] of previous object [%s]): package %s%n",
                previous.getClass(),
                previous,
                previous.getClass().getPackageName()
        );

        List<Method> methodList = Arrays.stream(aPackage.getClass().getMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> !method.getReturnType().equals(Void.TYPE))
                .collect(Collectors.toList());

        if (information_types.stream().anyMatch(s -> s.equals(package_info))){
            methodList = methodList.stream()
                    .filter(method -> method.getName().matches(String.format(".*%s.*", package_info)))
                    .collect(Collectors.toList());
        }

        methodList.forEach(method -> {
            try {
                Object result = method.invoke(aPackage);

                if (result == null) {
                    out.printf("%s: Result Non Defined%n", method.getName());
                }
                else if (result.getClass().isArray() && ((Object[])result).length == 0){
                    out.printf("%s: %n", method.getName());
                }
                else {
                    out.printf("%s: %s%n", method.getName(), result);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void callGenericCommand(String command) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        out.println("Trying generic command " + command);
        String method_args = sc.nextLine();
        if (method_args.isEmpty()){
            Method method = Objects.requireNonNull(previous).getClass().getMethod(command);
            previous = method.invoke(previous);
        }
        else {
            // only accept one int for now
            String[] args = method_args.trim().split(" ");

            if (args.length % 2 == 0){
                Class[] types = getTypesFromArgs(args);

                Method method;
                if (previous instanceof Class<?>){
                    method = ((Class) previous).getMethod(command, types);
                }
                else{
                    method = Objects.requireNonNull(previous).getClass().getMethod(command, types);
                }

                Object[] params = getParamsFromArgs(args);

                previous = method.invoke(previous, params);
            }
        }
    }

    private static Object[] getParamsFromArgs(String[] args) {
        return IntStream.range(0, args.length)
                            .filter(i -> i % 2 != 0)
                            .mapToObj(i -> {
                                if (PRIMITIVES_MAP.containsKey(args[i-1])){
                                    try {
                                        return PRIMITIVES_MAP.get(args[i-1]).getMethod("valueOf", String.class).invoke(PRIMITIVES_MAP.get(args[i-1]), args[i]);
                                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    return Class.forName(args[i - 1]).getConstructor(String.class).newInstance(args[i]);
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            })
                            .toArray();
    }

    private static Class[] getTypesFromArgs(String[] args) {
        return IntStream.range(0, args.length)
                            .filter(i -> i % 2 == 0)
                            .mapToObj(i -> {
                                if (PRIMITIVES_CLASSES.containsKey(args[i])){
                                    return PRIMITIVES_CLASSES.get(args[i]);
                                }
                                else{
                                    try {
                                        return Class.forName(args[i]);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return null;
                            }).toArray(Class[]::new);
    }
}
