/*
 *  To develop pamguard plugin try the GUI awt package in java 
 *  Shane
 *  2020_12_28
 */
package AWT;
import java.awt.*;
import java.awt.event.*;

public class PopupMenuExample implements MouseListener, ActionListener{
    PopupMenu p_menu;
    Frame f;
    PopupMenuExample(){
        p_menu = new PopupMenu();
        f = new Frame("PopupMenu Example");
        Menu m1 = new Menu("Outside");
        MenuItem m2 = new MenuItem("Cut");
        MenuItem m3 = new MenuItem("Copy");
        MenuItem m4 = new MenuItem("Paste");
        MenuItem m1_1 = new MenuItem("Inside");

        m2.setActionCommand("cut");
        m2.addActionListener(this);

        m3.setActionCommand("copy");
        m3.addActionListener(this);
        
        m4.setActionCommand("paste");
        m4.addActionListener(this);
        
        m1_1.setActionCommand("inside");
        m1_1.addActionListener(this);

        m1.add(m1_1);
        p_menu.add(m1);
        p_menu.add(m2);
        p_menu.add(m3);
        p_menu.add(m4);

        f.setSize(300,300);
        f.setVisible(true);
        f.setLayout(null);
        f.add(p_menu);
        f.addMouseListener(this);
    }
    public void mouseClicked(MouseEvent e){
        p_menu.show(f,e.getX(),e.getY());
    }
    public void mouseEntered(MouseEvent e) {}  
    public void mouseExited(MouseEvent e) {}  
    public void mousePressed(MouseEvent e) {}  
    public void mouseReleased(MouseEvent e) {} 

    public void actionPerformed(ActionEvent e){
        String cmd = e.getActionCommand();
        if(cmd=="cut"){
            System.out.println("cut is selected");
        }
        if(cmd=="copy"){
            System.out.println("copy is selected");
        }
        if(cmd=="paste"){
            System.out.println("paste is selected");
        }
        if(cmd=="inside"){
            System.out.println("inside is selected");
        }
    }

    public static void main(String[] args){
        new PopupMenuExample();
    }
}