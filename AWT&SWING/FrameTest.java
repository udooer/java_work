/*
 *  To develope pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_25
 */

package AWT;

import java.awt.*;

public class FrameTest extends Frame{
	FrameTest(){
		Button b = new Button("Click me");
		b.setBounds(5, 50, 80, 30);
		add(b);
		setSize(300,300);
		setLayout(null);
		setVisible(true);
	}
	public static void main(String[] args){
		FrameTest f = new FrameTest();
	}
}