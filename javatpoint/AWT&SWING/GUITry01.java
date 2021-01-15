package GUI;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

public class GUITry01{
    JFrame f;
    JPanel p;
    JTextField t;
    JLabel l;
    JButton b;
    JComboBox combo;

    GUITry01(){
        f = new JFrame("Audio Data Acquisition");
        p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,4,2,4);

        p.setBorder(new TitledBorder("Server Settings"));

/******************** First Row ***********************/
        p.add(l = new JLabel("Net Address"), c);
        l.setHorizontalAlignment(JLabel.RIGHT);
        c.gridx++;

        p.add(new JTextField("172.16.0.107", 15), c);
        c.gridx++;

        p.add(l = new JLabel("Connection Timeout:", JLabel.LEFT), c);
        l.setHorizontalAlignment(JLabel.LEFT);
        c.gridx++;

        p.add(new JTextField("", 6), c);
        c.gridx++;

        p.add(l = new JLabel("ms"), c);
        l.setHorizontalAlignment(JLabel.LEFT);
        c.gridx = 0;
        c.gridy++;

/******************** Second Row ***********************/
        p.add(l = new JLabel("Command Port"), c);
        l.setHorizontalAlignment(JLabel.RIGHT);
        c.gridx++;


        p.add(t = new JTextField("50000"), c);
        t.setEditable(false);
        c.gridx++;

        p.add(l = new JLabel("WaveForm Port:"), c);
        l.setHorizontalAlignment(JLabel.LEFT);
        c.gridx++;

        p.add(t = new JTextField("51678"), c);
        t.setEditable(false);
        c.gridx = 0;
        c.gridy++;

/******************** Third Row ***********************/
        p.add(l = new JLabel("Wave Form BandWidth"), c);
        l.setHorizontalAlignment(JLabel.RIGHT);
        c.gridx++;

        String[] s = {"400 Hz", "800 Hz", "1600 Hz", "3200 Hz", "6400 Hz",
                      "12.8 kHz", "25.6 kHz", "51.2 kHz", "102.4 kHz",
                      "204.8 kHz"};
        p.add(combo = new JComboBox(s), c);
        c.gridx++;

        p.add(l = new JLabel("Gives a sample rate of:"), c);
        l.setHorizontalAlignment(JLabel.LEFT);
        c.gridx++;

        p.add(t = new JTextField("1000"), c);
        t.setEditable(false);
        c.gridx++;

        p.add(l = new JLabel("hz"), c);
        l.setHorizontalAlignment(JLabel.LEFT);
        c.gridx = 0;
        c.gridy++;
/******************** fouth Row ***********************/
        p.add(t = new JTextField("No icListen Device Connected", 20), c);
        t.setHorizontalAlignment(JTextField.RIGHT);
        t.setEditable(false);
        c.gridx++;

        p.add(b = new JButton("connect"), c);
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.out.println("press the connect button");
            }
        });
        c.gridx++;

        p.add(b = new JButton("disconnect"), c);
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.out.println("press the disconnect button");
            }
        });
        b.setEnabled(false);

        f.add(p);
        f.pack();
        f.setVisible(true);
    }

    public static void main(String[] args){
        new GUITry01();
    }
}

