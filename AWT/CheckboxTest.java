/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_27
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class CheckboxTest implements ItemListener{  
    Label l1;
    Checkbox c_b1;
    Checkbox c_b2;
    Frame f;
    CheckboxTest(){
        f = new Frame("CheckBox Example");
        l1 = new Label();
        c_b1 = new Checkbox("C++");
        c_b2 = new Checkbox("Java");

        l1.setBounds(30,30,240,60);

        c_b1.setBounds(100,100,80,30);
        c_b1.addItemListener(this);

        c_b2.setBounds(100,180,80,30);
        c_b2.addItemListener(this);

        f.setSize(300,300);
        f.setVisible(true);
        f.setLayout(null);
        f.add(c_b1);
        f.add(c_b2);
        f.add(l1);
    }
    public void itemStateChanged(ItemEvent ie){
        String item = (String)ie.getItem();
        if(ie.getStateChange()==ItemEvent.SELECTED){
            String l = l1.getText();
            l += (item + " is selected.");
            l1.setText(l);
        }
        if(ie.getStateChange()==ItemEvent.DESELECTED){
            String l = l1.getText();
            int item_len = item.length();
            String l_out="";
            String arr[] = l.split("\\.");
            
            for(String i:arr){
                if(i.length()!=0 && i.substring(0,item_len).equals(item)){
                    continue;
                }
                l_out += (i+".");
            }
            l1.setText(l_out);
        }
    }
    public static void main(String[] args){
        new CheckboxTest();
    }
}