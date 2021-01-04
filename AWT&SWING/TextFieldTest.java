/*
 *  To develope pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_26
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class TextFieldTest implements ActionListener{
    TextField tf1;
    TextField tf2;
    TextField tf3;
    Button b1;
    Button b2;
    Frame f;
    TextFieldTest(){
        f = new Frame("textField example");
        tf1 = new TextField();
        tf2 = new TextField();
        tf3 = new TextField();
        b1 = new Button("+");
        b2 = new Button("-");

        b1.setBounds(50,200,50,50);
        b1.setActionCommand("b1");
        b1.addActionListener(this);
        b2.setBounds(200,200,50,50);
        b2.setActionCommand("b2");
        b2.addActionListener(this);

        tf1.setBounds(100,50,100,30);
        tf2.setBounds(100,100,100,30);
        tf3.setBounds(100,150,100,30);

        f.setSize(300,300);
        f.setVisible(true);
        f.setLayout(null);
        f.add(tf1);
        f.add(tf2);
        f.add(tf3);
        f.add(b1);
        f.add(b2);
    }
    public void actionPerformed(ActionEvent e){
        String cmd = e.getActionCommand();
        int a;
        int b;
        if(cmd=="b1"){
            a = Integer.valueOf(tf1.getText());
            b = Integer.valueOf(tf2.getText());
            tf3.setText(String.valueOf(a+b));
        }
        if(cmd=="b2"){
            a = Integer.valueOf(tf1.getText());
            b = Integer.valueOf(tf2.getText());
            tf3.setText(String.valueOf(a-b));
        }
    }
    public static void main(String[] args){
        new TextFieldTest();
    }

}