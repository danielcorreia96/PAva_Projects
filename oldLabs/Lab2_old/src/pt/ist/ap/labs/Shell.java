package pt.ist.ap.labs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.out;

public class Shell {
    private static Object previous_result = null;
    private static Scanner sc = new Scanner(System.in);

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
