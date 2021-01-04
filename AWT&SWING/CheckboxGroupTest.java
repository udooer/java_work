/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_27
 */
package AWT;
import java.awt.*;
import java.awt.event.*;
public class CheckboxGroupTest implements ItemListener{
	Frame f;
	CheckboxGroup cbg;
	Checkbox cb1;
	Checkbox cb2;
	Label l;
	CheckboxGroupTest(){
		f = new Frame("CheckboxGroup Example");
		cbg = new CheckboxGroup();
		cb1 = new Checkbox(" C++",cbg,false);
		cb2 = new Checkbox(" Java",cbg,false);
		l = new Label();

		l.setBounds(50,50,150,30);

		cb1.setBounds(100,100,50,30);
		cb1.addItemListener(this);

		cb2.setBounds(100,180,50,30);
		cb2.addItemListener(this);

		f.setSize(300,300);
		f.setVisible(true);
		f.setLayout(null);
		f.add(l);
		f.add(cb1);
		f.add(cb2);
		
		
	}
	public void itemStateChanged(ItemEvent e){
		String item = (String)e.getItem();
		if(item.equals(" C++") && e.getStateChange()==ItemEvent.SELECTED){
			l.setText("C++ is selected.");
		}
		else if(item.equals(" Java") && e.getStateChange()==ItemEvent.SELECTED){
			l.setText("Java is selected.");
		}
		else
			l.setText("");

	}
	public static void main(String[] args){
		new CheckboxGroupTest();
	}
} 

