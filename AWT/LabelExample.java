/*
 *  To develope pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_27
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class LabelExample extends Frame implements ActionListener{
	Label l;
	Button b1;
	Button b2;
	TextField tf;
	LabelExample(){
		l = new Label("Hello");
		b1 = new Button("click");
		b2 = new Button("show");
		tf = new TextField();

		b1.setBounds(50,100,80,50);
		b1.addActionListener(this);
		b1.setActionCommand("b1");

		b2.setBounds(150,100,80,50);
		b2.addActionListener(this);
		b2.setActionCommand("b2");

		l.setBounds(50,200,200,30);

		tf.setBounds(100,50,100,30);

		add(b1);
		add(b2);
		add(tf);
		add(l);
		setSize(300,300);
		setLayout(null);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e){
		String cmd = e.getActionCommand();
		if(cmd=="b1"){
			l.setText("clicking the click button");
		}
		if(cmd=="b2"){
			String s = tf.getText();
			l.setText("showing the text: " + s);
		}
	}
	public static void main(String[] args){
		new LabelExample();
	}
}