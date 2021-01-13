package pamguard_ros_bridge;

/*
 *  This is the panel that will be embeded onto the
 *  acquisitionDialog panel, will be mainly used to 
 *  establish connection with ROS master
 *  Shane 
 *  2021_01_04  
 */

// import pamguard lib
import Acquisition.AcquisitionDialog;
import PamView.dialog.PamGridBagContraints;

// import awt lib
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// import swing lib
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ROSMsgDaqPanelV1 extends JPanel{
    private AcquisitionDialog acquisition_dialog;
    /* GUI components for ROSMsgDaqPanel */
    private JLabel label_node, label_topic, label_msg;
    private JTextField tf_node, tf_topic, tf_msg;
    private JCheckBox cb_anonymous, cb_lock;


    /*
     *  standard constructor for ROSMsgDaqPanel Functions: 
     *  init and setup the layout for ROSMsgDaqPanel
     *  establish connections with ROS master
     *  @param acquisitionDialog 
     */
    public ROSMsgDaqPanelV1(AcquisitionDialog acquisition_dialog){
        this.acquisition_dialog = acquisition_dialog;

        // get the access of Ok button in AcquisitionDialog
        JButton button_ok = acquisition_dialog.getOkButton();
        button_ok.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                System.out.println("Ok button pressed");
            }
        });

        // get the access of Cancel button in AcqusitionDialog
        JButton button_cancel = acquisition_dialog.getCancelButton();
        button_cancel.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                System.out.println("Cancel button pressed");
            }
        });

        //using a box layout to set up the views for ROSMsgDaqPanel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p_ros = new JPanel(new GridBagLayout());
        GridBagConstraints c = new PamGridBagContraints();
        p_ros.setBorder(new TitledBorder("ROS Settings"));

        //set up the view of layout
        p_ros.add(label_node = new JLabel("Node Name", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_node = new JTextField("pamguard_node", 30), c);
        tf_node.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(label_topic = new JLabel("Topic Subscribed", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_topic = new JTextField("/get_sound_data_for2i2/hydrophone_data"), c);
        tf_topic.setEditable(false);
        c.gridx = 0;
        c.gridy++;
        
        p_ros.add(label_msg = new JLabel("Message Name", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_msg = new JTextField("HydrophoneData.msg"), c);
        tf_msg.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(cb_anonymous = new JCheckBox("anonymous", true), c); 
        cb_anonymous.setEnabled(false);
        cb_anonymous.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                if(e.getStateChange()==ItemEvent.SELECTED)
                    System.out.println("anonymous is selected");
                if(e.getStateChange()==ItemEvent.DESELECTED)
                    System.out.println("anonymous is not selected");
            }
        });
        c.gridx = 0;
        c.gridy++;

        c.insets = new Insets(10,2,2,2);
        p_ros.add(cb_lock = new JCheckBox("Locked", true), c);
        cb_lock.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                if(e.getStateChange()==ItemEvent.SELECTED){
                    System.out.println("lock the layout");
                    tf_msg.setEditable(false);
                    tf_topic.setEditable(false);
                    tf_node.setEditable(false);
                    cb_anonymous.setEnabled(false);
                }
                if(e.getStateChange()==ItemEvent.DESELECTED){
                    System.out.println("free the layout");
                    tf_msg.setEditable(true);
                    tf_topic.setEditable(true);
                    tf_node.setEditable(true);
                    cb_anonymous.setEnabled(true);
                }
            }
        });

        this.add(p_ros);
    }

    public ROSMsgDaqPanelV1(){
        //using a box layout to set up the views for ROSMsgDaqPanel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p_ros = new JPanel(new GridBagLayout());
        GridBagConstraints c = new PamGridBagContraints();
        p_ros.setBorder(new TitledBorder("ROS Settings"));

        //set up the view of layout
        p_ros.add(label_node = new JLabel("Node Name", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_node = new JTextField("pamguard_node", 30), c);
        tf_node.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(label_topic = new JLabel("Topic Subscribed", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_topic = new JTextField("/get_sound_data_for2i2/hydrophone_data"), c);
        tf_topic.setEditable(false);
        c.gridx = 0;
        c.gridy++;
        
        p_ros.add(label_msg = new JLabel("Message Name", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_msg = new JTextField("HydrophoneData.msg"), c);
        tf_msg.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(cb_anonymous = new JCheckBox("anonymous", true), c); 
        cb_anonymous.setEnabled(false);
        cb_anonymous.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                if(e.getStateChange()==ItemEvent.SELECTED)
                    System.out.println("anonymous is selected");
                if(e.getStateChange()==ItemEvent.DESELECTED)
                    System.out.println("anonymous is not selected");
            }
        });
        c.gridx = 0;
        c.gridy++;

        c.insets = new Insets(10,2,2,2);
        p_ros.add(cb_lock = new JCheckBox("Locked", true), c);
        cb_lock.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                if(e.getStateChange()==ItemEvent.SELECTED){
                    System.out.println("lock the layout");
                    tf_msg.setEditable(false);
                    tf_topic.setEditable(false);
                    tf_node.setEditable(false);
                    cb_anonymous.setEnabled(false);
                }
                if(e.getStateChange()==ItemEvent.DESELECTED){
                    System.out.println("free the layout");
                    tf_msg.setEditable(true);
                    tf_topic.setEditable(true);
                    tf_node.setEditable(true);
                    cb_anonymous.setEnabled(true);
                }
            }
        });

        this.add(p_ros);
    }

    public static void main(String[] args){
        JFrame f = new JFrame("ROS Bridge GUI");
        JPanel p = new ROSMsgDaqPanelV1();
        f.add(p);
        f.pack();
        f.setVisible(true);
    }   
}





