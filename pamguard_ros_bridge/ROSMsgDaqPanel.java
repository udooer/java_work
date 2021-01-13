package pamguard_ros_bridge;

/*
 *  This is the version 2 panel that will be embeded 
 *  onto the acquisitionDialog panel, will be mainly 
 *  used to establish connection with rosbridge 
 *  server(ws://0.0.0.0:9090)
 *  Shane 
 *  2021_01_13
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

//import net lib
import java.net.URI;
import java.net.URISyntaxException;

//import java_websocker lib
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

//import JSON lib
import org.json.JSONException;
import org.json.JSONObject;


public class ROSMsgDaqPanel extends JPanel{
    private AcquisitionDialog acquisition_dialog;
    /* GUI components for ROSMsgDaqPanel */
    private JLabel label_server, label_topic, label_msg;
    private JTextField tf_server, tf_topic, tf_msg, tf_status;
    private JCheckBox cb_lock;
    private JButton b_connect, b_disconnect;

    private ROSMsgParams params;


    /*
     *  standard constructor for ROSMsgDaqPanel Functions: 
     *  init and setup the layout for ROSMsgDaqPanel
     *  establish connections with rosbridge server
     *  @param acquisitionDialog 
     */
    public ROSMsgDaqPanel(AcquisitionDialog acquisition_dialog){
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
        params = new ROSMsgParams();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p_ros = new JPanel(new GridBagLayout());
        GridBagConstraints c = new PamGridBagContraints();
        p_ros.setBorder(new TitledBorder("ROS Bridge Server Settings"));

        //set up the view of layout
        p_ros.add(label_server = new JLabel("WebSocket Server", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_server = new JTextField("ws://0.0.0.0:9090", 25), c);
        tf_server.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(label_topic = new JLabel("Topic Subscribed", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_topic = new JTextField("/get_sound_data_for2i2/hydrophone_data"), c);
        tf_topic.setEditable(false);
        c.gridx = 0;
        c.gridy++;
        
        p_ros.add(label_msg = new JLabel("Message Type", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_msg = new JTextField("ntu_msgs/HydrophoneData"), c);
        tf_msg.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(tf_status = new JTextField("WebSocket Client Disconnect", 20), c);
        tf_status.setEditable(false);
        c.gridx++;
        p_ros.add(b_connect = new JButton("connect"), c);
        b_connect.setEnabled(true);
        b_connect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String server_name = tf_server.getText();
                try{
                    params.m_ws = new WebSocketClient(new URI(server_name), new Draft_6455()){
                        @Override
                        public void onMessage(String message){
                            JSONObject obj = new JSONObject(message);
                            System.out.println(obj);
                        }

                        @Override
                        public void onOpen(ServerHandshake handshake){
                            System.out.println("opened connection");
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote){
                            System.out.println( "closed connection" );
                        }

                        @Override
                        public void onError(Exception ex){
                            ex.printStackTrace();
                        }
                    };
                    params.m_status = true;
                }catch(URISyntaxException uri_e){
                    System.out.println(uri_e);
                }
                if(params.m_status){
                    params.m_ws.connect();
                    try{
                        Thread.sleep(1000);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    JSONObject obj = new JSONObject();
                    String topic_name = tf_topic.getText();
                    String topic_type = tf_msg.getText();
                    obj.put("op", "subscribe");
                    obj.put("topic", topic_name);
                    obj.put("type", topic_type);
                    String message = obj.toString();
                    //send message
                    params.m_ws.send(message);
                    System.out.println("subscribed message sent");
                    b_connect.setEnabled(false);
                    tf_status.setText("WebSocket Client Connect Successfully");
                    b_disconnect.setEnabled(true);
                }
            }
        });
        c.gridx++;
        p_ros.add(b_disconnect = new JButton("disconnect"), c);
        b_disconnect.setEnabled(false);
        b_disconnect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                params.m_ws.close();
                params.m_status = false;
                b_connect.setEnabled(true);
                b_disconnect.setEnabled(false);
                tf_status.setText("WebSocket Client Disconnect");
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
                    tf_server.setEditable(false);
                }
                if(e.getStateChange()==ItemEvent.DESELECTED){
                    System.out.println("free the layout");
                    tf_msg.setEditable(true);
                    tf_topic.setEditable(true);
                    tf_server.setEditable(true);
                }
            }
        });

        this.add(p_ros);
    }

    public ROSMsgDaqPanel(){
        //using a box layout to set up the views for ROSMsgDaqPanel
        params = new ROSMsgParams();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p_ros = new JPanel(new GridBagLayout());
        GridBagConstraints c = new PamGridBagContraints();
        p_ros.setBorder(new TitledBorder("ROS Bridge Server Settings"));

        //set up the view of layout
        p_ros.add(label_server = new JLabel("WebSocket Server", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_server = new JTextField("ws://0.0.0.0:9090", 25), c);
        tf_server.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(label_topic = new JLabel("Topic Subscribed", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_topic = new JTextField("/get_sound_data_for2i2/hydrophone_data"), c);
        tf_topic.setEditable(false);
        c.gridx = 0;
        c.gridy++;
        
        p_ros.add(label_msg = new JLabel("Message Type", JLabel.RIGHT), c);
        c.gridx++;
        p_ros.add(tf_msg = new JTextField("ntu_msgs/HydrophoneData"), c);
        tf_msg.setEditable(false);
        c.gridx = 0;
        c.gridy++;

        p_ros.add(tf_status = new JTextField("WebSocket Client Disconnect", 20), c);
        tf_status.setEditable(false);
        c.gridx++;
        p_ros.add(b_connect = new JButton("connect"), c);
        b_connect.setEnabled(true);
        b_connect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                String server_name = tf_server.getText();
                try{
                    params.m_ws = new WebSocketClient(new URI(server_name), new Draft_6455()){
                        @Override
                        public void onMessage(String message){
                            JSONObject obj = new JSONObject(message);
                            System.out.println(obj);
                        }

                        @Override
                        public void onOpen(ServerHandshake handshake){
                            System.out.println("opened connection");
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote){
                            System.out.println( "closed connection" );
                        }

                        @Override
                        public void onError(Exception ex){
                            ex.printStackTrace();
                        }
                    };
                    params.m_status = true;
                }catch(URISyntaxException uri_e){
                    System.out.println(uri_e);
                }
                if(params.m_status){
                    params.m_ws.connect();
                    try{
                        Thread.sleep(1000);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    JSONObject obj = new JSONObject();
                    String topic_name = tf_topic.getText();
                    String topic_type = tf_msg.getText();
                    obj.put("op", "subscribe");
                    obj.put("topic", topic_name);
                    obj.put("type", topic_type);
                    String message = obj.toString();
                    //send message
                    params.m_ws.send(message);
                    System.out.println("subscribed message sent");
                    b_connect.setEnabled(false);
                    tf_status.setText("WebSocket Client Connect Successfully");
                    b_disconnect.setEnabled(true);
                }
            }
        });
        c.gridx++;
        p_ros.add(b_disconnect = new JButton("disconnect"), c);
        b_disconnect.setEnabled(false);
        b_disconnect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                params.m_ws.close();
                params.m_status = false;
                b_connect.setEnabled(true);
                b_disconnect.setEnabled(false);
                tf_status.setText("WebSocket Client Disconnect");
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
                    tf_server.setEditable(false);
                }
                if(e.getStateChange()==ItemEvent.DESELECTED){
                    System.out.println("free the layout");
                    tf_msg.setEditable(true);
                    tf_topic.setEditable(true);
                    tf_server.setEditable(true);
                }
            }
        });

        this.add(p_ros);
    }

    public static void main(String[] args){
        JFrame f = new JFrame("ROS Bridge GUI");
        JPanel p = new ROSMsgDaqPanel();
        f.add(p);
        f.pack();
        f.setVisible(true);
    }   
}





