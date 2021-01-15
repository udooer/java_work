/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_27
 */
package AWT;
import java.awt.*;
import java.awt.event.*;
public class TextAreaTest implements ActionListener{
	Frame f;
	Button b;
	Label l1;
	Label l2;
	TextArea t_a;

	TextAreaTest(){
		f = new Frame("TextArea Example");
		b = new Button("count");
		l1 = new Label("Words:");
		l2 = new Label("Characters:");
		t_a = new TextArea();

		b.setBounds(165,400,70,50);
		b.addActionListener(this);
		b.setActionCommand("b");

		l1.setBounds(50,50,100,30);
		l2.setBounds(200,50,100,30);

		t_a.setBounds(50,100,300,300);

		f.setSize(400,450);
		f.setVisible(true);
		f.setLayout(null);
		f.add(b);
		f.add(l1);
		f.add(l2);
		f.add(t_a);
	}
	public void actionPerformed(ActionEvent e){
		String cmd = e.getActionCommand();
		if(cmd=="b"){
			String t = t_a.getText();
			String s_a[] = t.split("\\s|\\.");
			l1.setText("Words:" + s_a.length);
			l2.setText("Chracters: " + t.length());
		}
	}
	public static void main(String[] args){
		new TextAreaTest();
	}
} 