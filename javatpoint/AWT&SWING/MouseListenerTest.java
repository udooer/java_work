/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_29
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class MouseListenerTest implements MouseListener{
	Frame f;
	Label l;

	MouseListenerTest(){
		f = new Frame("MouseListener Example");
		l = new Label("");

		l.setBounds(100,100,100,30);
		
		f.setSize(300,300);
		f.setVisible(true);
		f.add(l);
		f.setLayout(null);
		f.addMouseListener(this);
	};

	public void mouseClicked(MouseEvent e) {  
        // l.setText("Mouse Clicked");  
    }  
    public void mouseEntered(MouseEvent e) {  
        l.setText("Mouse Entered");  
    }  
    public void mouseExited(MouseEvent e) {  
        l.setText("Mouse Exited");  
    }  
    public void mousePressed(MouseEvent e) {  
        l.setText("Mouse Pressed");  
    }  
    public void mouseReleased(MouseEvent e) {  
        l.setText("Mouse Released");  
    }

    public static void main(String[] args){
    	new MouseListenerTest();
    }   

}