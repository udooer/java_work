/*
 *  To develope pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_25
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class EventTest extends Frame implements ActionListener{
    TextField tf;
    EventTest(){
        Button b1 = new Button("click me");
        Button b2 = new Button("clear");
        tf = new TextField();
        
        b1.setBounds(50,80,80,50);
        b1.setActionCommand("b1");
        b1.addActionListener(this);

        b2.setBounds(150,80,80,50);
        b2.setActionCommand("b2");
        b2.addActionListener(this);

        tf.setBounds(100,40,100,20);

        setVisible(true);
        setSize(300,300);
        setLayout(null);
        add(tf);
        add(b1);
        add(b2);
    }
    public void actionPerformed(ActionEvent e){
        String cmd = e.getActionCommand();
        if(cmd == "b1"){
            tf.setText("Welcome!!!");
        }
        if(cmd == "b2"){
            tf.setText("");
        }
    }
    public static void main(String[] args){
        EventTest e_test = new EventTest();
    }
}