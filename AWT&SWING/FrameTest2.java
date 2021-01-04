/*
 *  To develope pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_25
 */
package AWT;

import java.awt.*;

class Frame2{
    Frame2(){
        Button b = new Button("click me");
        b.setBounds(50,50,80,30);
        
        Frame f = new Frame();
        f.setSize(300,300);
        f.setLayout(null);
        f.setVisible(true);
        f.add(b);
    }
}
public class FrameTest2{
    public static void main(String[] args){
        Frame2 f = new Frame2();
    }
}
