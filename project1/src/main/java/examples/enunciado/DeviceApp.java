package examples.enunciado;

import meic.ist.pa.GenericFunctions.GenericFunction;

class Shape {}

class Line extends Shape {}

class Circle extends Shape {}

@GenericFunction
class Device {
    static Object draw(Device d, Shape s){
        System.out.println("draw what where?");
        return "";
    }
    static Object draw(Device d, Line l){
        System.out.println("draw a line where?");
        return "";
    }
    static Object draw(Device d, Circle c){
        System.out.println("draw a circle where?");
        return "";
    }

    static Object draw(Screen d, Shape s){
        System.out.println("draw a what on screen?");
        return "";
    }
    static Object draw(Screen d, Line l){
        System.out.println("draw a line on screen!");
        return "";
    }
    static Object draw(Screen d, Circle c){
        System.out.println("draw a circle on screen!");
        return "";
    }
    static Object draw(Printer d, Shape s){
        System.out.println("draw what on printer?");
        return "";
    }
    static Object draw(Printer d, Line l){
        System.out.println("draw a line on printer!");
        return "";
    }
    static Object draw(Printer d, Circle c){
        System.out.println("draw a circle on printer!");
        return "";
    }

}

class Screen extends Device {}

class Printer extends Device {}

public class DeviceApp {

    public static void main(String[] args) {
        Device[] devices = new Device[] { new Screen(), new Printer() };
        Shape[] shapes = new Shape[] { new Line(), new Circle() };

        for (Device device : devices) {
            for (Shape shape : shapes) {
                Device.draw(device, shape);
            }
        }
    }
}
