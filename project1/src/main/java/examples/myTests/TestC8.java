package examples.myTests;

import examples.myTests.domain.Color;
import examples.myTests.domain.Red;

public class TestC8 {
    public static void main(String[] args) {
        Color[] colors = new Color[] { new Red(), new Blue(), new Black() };
        for (Color c : colors)
            System.out.println(Color.mix(c));  
            
        System.out.println("time for some fun");
        for (Color color : colors) {
            Red.doThings(color);
        }
    }
}