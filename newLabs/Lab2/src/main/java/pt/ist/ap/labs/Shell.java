package pt.ist.ap.labs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Shell {
    private static Object previous = null;
    private static Scanner sc = new Scanner(System.in);
    private static Map<String,Object> objectMap = new HashMap<>();

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
                out.println(e.getMessage());
            }
            printPreviousResult();
        }
    }

    private static void setVariable() {
        objectMap.put(sc.next(), previous);
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
            out.println(previous);
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

                if (result == null)
                    out.printf("%s: Result Non Defined%n", method.getName());
                else
                    out.printf("%s: %s%n", method.getName(), result);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void callGenericCommand(String command) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        out.println("Trying generic command " + command);
        String method_args = sc.nextLine();
        Method method = Objects.requireNonNull(previous).getClass().getMethod(command);
        if (method_args.isEmpty()){
            previous = method.invoke(previous);
        }
        else {
            previous = method.invoke(previous, (Object) method_args.split(" "));
        }
    }
}
