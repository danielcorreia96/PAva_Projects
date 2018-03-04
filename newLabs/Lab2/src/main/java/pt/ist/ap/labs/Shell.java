package pt.ist.ap.labs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Shell {
    private static Object previous_result = null;
    private static Scanner sc = new Scanner(System.in);
    private static List<String> information_types = Arrays.asList("Implementation", "Annotation", "Specification", "Name");

    public static void main(String... args){
        boolean active_shell = true;
        Map<String,Object> objectMap = new HashMap<>();
        while (active_shell){
            out.print("Command:> ");
            String command = sc.next();
            switch (command) {
                case "Class":
                    String class_name = sc.next();
                    try {
                        previous_result = Class.forName(class_name);
                        out.println(previous_result);
                    } catch (ClassNotFoundException e) {
                        out.println("Unable to find class " + class_name);
                    }
                    break;

                case "Set":
                    objectMap.put(sc.next(),previous_result);
                    out.println("Saved name for object of type " + previous_result.getClass());
                    out.println(previous_result);
                    break;

                case "Get":
                    previous_result = objectMap.get(sc.next());
                    out.println(previous_result);
                    break;

                case "Index":
                    previous_result = ((Object[]) Objects.requireNonNull(previous_result))[Integer.parseInt(sc.next())];
                    out.println(previous_result);
                    break;

                case "Package":
                    String package_info = sc.nextLine().trim();
                    out.print("Package information (of ["+ previous_result.getClass() + "]");
                    out.print(" of previous object [" + previous_result +"]):");
                    out.println(" package " + previous_result.getClass().getPackageName());
                    printPackageInformation(previous_result.getClass().getPackage(), package_info);
                    break;

                case "Exit":
                    out.println("Closing introspection shell");
                    active_shell = false;
                    break;

                default:
                    out.println("Trying generic command " + command);
                    callGenericCommand(command);
                    if (previous_result.getClass().isArray()){
                        for (Object o: (Object[]) previous_result) {
                            out.println(o);
                        }
                    }
                    else {
                        out.println(previous_result);
                    }
                    break;
            }
        }
    }

    private static void printPackageInformation(Package aPackage, String package_info) {
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
                out.print(method.getName() + ": ");
                Object result = method.invoke(aPackage);

                if (result == null) out.println("Result Non Defined");
                else out.println(result);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void callGenericCommand(String command) {
        try {
            String method_args = sc.nextLine();
            Method method = Objects.requireNonNull(previous_result).getClass().getMethod(command);
            if (method_args.isEmpty()){
                previous_result = method.invoke(previous_result);
            }
            else {
                previous_result = method.invoke(previous_result, (Object) method_args.split(" "));
            }
        } catch (NoSuchMethodException e) {
            out.println("Unable to find method " + e.getMessage());
        } catch (IllegalAccessException e) {
            out.println("Unable to access method/constructor of the given class");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            out.println("Exception thrown by underlying invoked method");
            e.printStackTrace();
        }
    }
}
