package pt.ist.ap.labs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public class Application {

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.print("Please write a name of class in package: ");
        String input_string = "pt.ist.ap.labs" + "." + sc.next();

        try {
            Class<?> in_cls = Class.forName(input_string);
            Constructor in_ctr = in_cls.getConstructor();
            Object in_inst = in_ctr.newInstance();
            Method in_say_mtd = in_cls.getMethod("say");
            in_say_mtd.invoke(in_inst);
        } catch (ClassNotFoundException e) {
           System.out.println("Unable to find class " + input_string);
        } catch (NoSuchMethodException e) {
            System.out.println("Unable to find method " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println("Unable to access method/constructor of the given class");
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.out.println("Unable to create instance of an abstract class");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.out.println("Exception thrown by underlying invoked method");
            e.printStackTrace();
        }
    }
}
