/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_27
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class ChoiceTest implements ActionListener{
	Frame f; 
	Choice c; 
	Button b;
	Label l; 
	ChoiceTest(){
		f = new Frame("Choice Test");
		c = new Choice();
		b = new Button("show");
		l = new Label();

		c.setBounds(50,150,80,30);
		c.add("C++");
		c.add("Java");
		c.add("Python");
		c.add("GO");
		c.add("R");
		c.add("Shell Script");

		b.setBounds(160,150,50,30);
		b.addActionListener(this);

		l.setBounds(80,80,150,30);

		f.setSize(300,300);
		f.setVisible(true);
		f.setLayout(null);
		f.add(c);
		f.add(b);
		f.add(l);
	}
	public void actionPerformed(ActionEvent e){
		String s = c.getItem(c.getSelectedIndex());
		l.setText(s + " is selected.");
	}
	public static void main(String[] args){
		new ChoiceTest();
	}
}