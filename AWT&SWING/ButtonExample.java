/*
 *  To develope pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_26
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class ButtonExample{
	ButtonExample(){
		Frame f = new Frame("Button Example");
		Button b1 = new Button("click me");
		Button b2 = new Button("clear");
		TextField tf = new TextField();
		ActionListener a_l = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String cmd = e.getActionCommand();
				if(cmd=="b1"){
					tf.setText("welcome");
				}
				if(cmd=="b2"){
					tf.setText("");
				}
			}
		};

		b1.setBounds(50,80,80,50);
		b1.setActionCommand("b1");
		b1.addActionListener(a_l);

        b2.setBounds(150,80,80,50);
        b2.setActionCommand("b2");
        b2.addActionListener(a_l);

        tf.setBounds(100,40,100,20);

        f.setVisible(true);
        f.setSize(300,300);
        f.setLayout(null);
        f.add(tf);
        f.add(b1);
        f.add(b2); 
	}
	public static void main(String[] args){
		ButtonExample b_e = new ButtonExample();
	}
}