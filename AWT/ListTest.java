/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_27
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class ListTest implements ActionListener{
    Frame f;
    Button b;
    List l1;
    List l2;
    Label l;
    ListTest(){
        f = new Frame("List Example");
        b = new Button("show");
        l1 = new List(3,false);
        l2 = new List(3, true);
        l = new Label();

        b.setBounds(180,150,50,30);
        b.addActionListener(this);

        l1.setBounds(50,100,70,60);
        l1.add("C++");
        l1.add("Python");
        l1.add("Java");

        l2.setBounds(50,200,70,60);
        l2.add("Shane");
        l2.add("Logan");
        l2.add("Mireille");

        l.setBounds(50,50,500,30);

        f.setSize(600,450);
        f.setVisible(true);
        f.setLayout(null);
        f.add(b);f.add(l1);f.add(l2);f.add(l);
    }
    public void actionPerformed(ActionEvent e){
        String l_1 = l1.getItem(l1.getSelectedIndex());
        String[] l_2 = l2.getSelectedItems();
        String s = "Programming language selected: ";
        s += l_1;
        s += ", Name selected: ";
        for(String i:l_2){
            s += (i+" ");
        } 
        l.setText(s);
    }
    public static void main(String[] args){
        new ListTest();
    }
}
