package examples.cachedTests;

import ist.meic.pa.GenericFunctions.GenericFunction;

class Shape {}

class Line extends Shape {}

class Circle extends Shape {}

@GenericFunction
class Device {
    static void draw(Device d, Shape s){
        System.out.println("draw what where?");
    }
    static void draw(Device d, Line l){
        System.out.println("draw a line where?");
    }
    static void draw(Device d, Circle c){
        System.out.println("draw a circle where?");
    }

    static void draw(Screen d, Shape s){
        System.out.println("draw a what on screen?");
    }
    static void draw(Screen d, Line l){
        System.out.println("draw a line on screen!");
    }
    static void draw(Screen d, Circle c){
        System.out.println("draw a circle on screen!");
    }
    static void draw(Printer d, Shape s){
        System.out.println("draw what on printer?");
    }
    static void draw(Printer d, Line l){
        System.out.println("draw a line on printer!");
    }
    static void draw(Printer d, Circle c){
        System.out.println("draw a circle on printer!");
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

        System.out.println("\nNext method calls should use cached results\n");

        for (Device device : devices) {
            for (Shape shape : shapes) {
                Device.draw(device, shape);
            }
        }
    }
}
