/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_28
 */

package AWT;
import java.awt.*;
import java.awt.event.*;
public class DialogExample implements ActionListener{
	Frame f;
	Label l1;
	Label l2;
	Dialog d;
	Button b1;
	Button b2;

	DialogExample(){
		f = new Frame("Dialog Example");
		d = new Dialog(f,"Dialog",true);
		
		b1 = new Button("Chose");
		b2 = new Button("OK");
		

		l1 = new Label("Click button to pop out the dialog.");
		l2 = new Label("Click button to continue.");

		l1.setBounds(75,100,250,30);

		l2.setBounds(75,100,150,30);
		
		b1.setBounds(175,200,50,50);
		b1.addActionListener(this);
		b1.setActionCommand("chose");

		b2.setBounds(125,180,50,50);
		b2.addActionListener(this);
		b2.setActionCommand("ok");

		d.setSize(300,300);
		d.setVisible(false);
		d.setLayout(null);
		d.add(l2);
		d.add(b2);

		f.setSize(400,400);
		f.setVisible(true);
		f.setLayout(null);
		f.add(l1);
		f.add(b1);
	}
	public void actionPerformed(ActionEvent e){
		String cmd = e.getActionCommand();
		if(cmd=="chose"){
			d.setVisible(true);
		}
		if(cmd=="ok"){
			d.setVisible(false);
		}
	}
	public static void main(String[] args){
		new DialogExample();
	}
}
