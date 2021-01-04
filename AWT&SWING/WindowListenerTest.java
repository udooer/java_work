/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_29
 */
package AWT;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowListenerTest implements WindowListener{
	Frame f;
	WindowListenerTest(){
		f = new Frame();

		f.addWindowListener(this);
		f.setSize(300,300);
		f.setVisible(true);
	}

	public void windowActivated(WindowEvent arg0) {  
    	System.out.println("activated");  
	}  
	public void windowClosed(WindowEvent arg0) {  
    	System.out.println("closed");  
	}  
	public void windowClosing(WindowEvent arg0) {  
    	System.out.println("closing");  
    	f.dispose();  
	}  
	public void windowDeactivated(WindowEvent arg0) {  
    	System.out.println("deactivated");  
	}  
	public void windowDeiconified(WindowEvent arg0) {  
    	System.out.println("deiconified");  
	}  
	public void windowIconified(WindowEvent arg0) {  
    	System.out.println("iconified");  
	}  
	public void windowOpened(WindowEvent arg0) {  
    	System.out.println("opened");  
	}

	public static void main(String[] args){
		new WindowListenerTest();
	}
}