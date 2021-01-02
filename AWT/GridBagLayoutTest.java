/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_01_02
 */

package AWT;
import java.awt.*;

import javax.swing.JButton;
import javax.swing.JFrame;

public class GridBagLayoutTest{

    JFrame f; 
    GridBagLayoutTest(){
        f = new JFrame("GridBagLayout Example");
        f.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JButton b;
        b = new JButton("Button 1");
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        f.add(b, c);

        b = new JButton("Button 2");
        c.weightx=0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        f.add(b, c);

        b = new JButton("Button 3");
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        f.add(b, c);

        b = new JButton("Long-Named Button 4");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 40;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 1;
        f.add(b, c);

        b = new JButton("5");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0;
        c.anchor = GridBagConstraints.PAGE_END;
        c.weightx = 0;
        c.weighty = 1;
        c.insets = new Insets(10,0,0,0);
        c.gridx = 1;
        c.gridwidth = 2;
        c.gridy = 2;
        f.add(b, c);

        f.pack();
        f.setVisible(true);        
    }
    public static void main(String[] args){
        new GridBagLayoutTest();
    } 
}