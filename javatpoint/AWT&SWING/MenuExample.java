/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_28
 */

package AWT;
import java.awt.*;
import java.awt.event.*;

public class MenuExample{
	MenuExample(){
		Frame f = new Frame("Menu Example");
		MenuBar mb = new MenuBar();
		Menu m1 = new Menu("File");
		Menu m2 = new Menu("Open Recent");
		MenuItem m3 = new MenuItem("New File");
		MenuItem m4 = new MenuItem("Word");

		m2.add(m4);
		
		m1.add(m2);
		m1.add(m3);

		mb.add(m1);

		f.setSize(300,300);
		f.setMenuBar(mb);
		f.setLayout(null);
		f.setVisible(true);
	}
	public static void main(String[] args){
		new MenuExample();
	}
}