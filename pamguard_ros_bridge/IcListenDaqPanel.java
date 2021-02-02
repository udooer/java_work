package icListenPluginDaqBETA3_4;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import PamView.PamSlider;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;

/**
 * This is the panel that will be embedded onto the acquistionDialog panel, will
 * be mainly used to establish connection with the icListen.
 */
public class IcListenDaqPanel extends JPanel {
  // the constants below will be used for jobsetup()functionality.
  private final char SYNC_BYTE = 0x2A;
  private final char JOB_SETUP_COMMAND = 0x44;
  private final char NUMBER_OF_TAGS = 5;
  private final char JOB_SETUP_TYPE = 20;
  private final char TAG_VAL_LEN = 4;
  private final int MIN_MESSAGE_LEN = 6;

  private final char SAMPLE_RATE_TAG = 14;

  private final char BIT_DEPTH_TAG = 15;
  protected final int BIT_DEPTH_VALUE = 16;

  private final char GAIN_TAG = 16;
  private final int GAIN_VALUE = 0;

  private final char ENDIAN_TAG = 17;
  private final int ENDIAN_VALUE = 0;

  private final char LOGGING_MODE_TAG = 18;
  private final int LOGGING_MODE_VAL = 0;

  private final char MIN_TAG_LEN = 4;
  
  private final char JOB_SETUP_PAYLOAD_LEN = 4 + ((MIN_TAG_LEN + TAG_VAL_LEN) * NUMBER_OF_TAGS);

  static int payloadLength1;
  private AcquisitionDialog acquisitionDialog;
  private icListenDaq iClistenDaq;

  /* Gui components for IcListenDaqPanel */
  private JTextField ipAddress, tcpPort, timeout, sampleRate, connectionToDevice, waveFormPort;

  private JLabel connectionTimeOut, hz, ms, waveFormBandWidth, gives_SampleRate, waveForm;

  private JComboBox jcWaveFormBandWidth;

  private JButton connect, disconnect;

  private IcListenParams icListenParams;
  

  /**
   * standard constructor for IcListenDaqPanel Functions: initializing and set up
   * the layout for icListenDaqPanel; buttons; establish connections
   * with stream port and command_control port of the icListen System
   * 
   * @param acquisitionDialog
   * @param iCListenDaq
   */
  public IcListenDaqPanel(AcquisitionDialog acquisitionDialog, icListenDaq iCListenDaq,
      IcListenParams icListenParams) {
    this.acquisitionDialog = acquisitionDialog;

    this.icListenParams = icListenParams;
    
    //dont have direct access to pamguard's gui component, need to reference it this way
    //so we can do event listen on Ok button pressed.
    JButton jbPamOk = acquisitionDialog.getOkButton();
    jbPamOk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub
        System.out.println("Ok button pressed");
        if(icListenParams.connectionStatus.startsWith("connected"))
          icListenParams.connect = true;
      }
      
    });
    
    //same reason as the Button above.
    //disconnect everything on button closed pressed.
    JButton jbPamCancel = acquisitionDialog.getCancelButton();
    jbPamCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        System.out.println("On Cancel Button pressed.");
        icListenParams.connect = false;
        icListenParams.connectionStatus = "No icListen Device connected";
        connectionToDevice.setText(icListenParams.connectionStatus);

        connect.setEnabled(true);
        disconnect.setEnabled(false);
        jcWaveFormBandWidth.setEnabled(true);
        acquisitionDialog.readSampleRate();
        icListenParams.connect = false;
        acquisitionDialog.setSampleRate(icListenParams.Sample_Rate);
        try {
          if (icListenParams.commandSocket != null) {
            icListenParams.connect = false;
            icListenParams.commandSocket.close();
            icListenParams.streamSocket.close();
          }
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }

    });

    
    this.iClistenDaq = iClistenDaq;

    
    //first time using the icListenDaqPanel
    System.out.println("on initialization.");
    icListenParams.connectionStatus = "No icListen Device connected";
    icListenParams.Sample_Rate = 1000;
    icListenParams.connect = false;

    //forget what this thing does exactly.
    try {
      if (icListenParams.commandSocket != null) {
        icListenParams.connect = false;
        icListenParams.commandSocket.close();
        icListenParams.streamSocket.close();
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    //when intializing this thing the first time,reference of command and stream socket points to null
    System.out.println("command sockets sets to null, currently.");
    
    //using a box layout to set up the views for the icListen plugin daq panel.
    //just set up the gui components.
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel ipPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new PamGridBagContraints();
    ipPanel.setBorder(new TitledBorder("Server Settings"));
    ipPanel.add(new JLabel("Net Address ", JLabel.RIGHT), c);
    c.gridx++;
    ipPanel.add(ipAddress = new JTextField(15), c);
    ipAddress.setToolTipText("Default is 172.16.0.107");
    c.gridx++;
    ipPanel.add(connectionTimeOut = new JLabel("Connection Timeout: "));

    c.gridx++;
    ipPanel.add(timeout = new JTextField(6), c);
    timeout.setText("" + IcListenParams.TIMEOUT);
    ipPanel.add(ms = new JLabel("ms"));

    c.gridy++;
    c.gridx = 0;
    ipPanel.add(new JLabel("Command Port", JLabel.RIGHT), c);
    c.gridx++;
    ipPanel.add(tcpPort = new JTextField(6), c);
    tcpPort.setToolTipText("Default is 50000");
    tcpPort.setEditable(false);
    c.gridx++;
    ipPanel.add(waveForm = new JLabel("waveForm Port"), c);
    c.gridx++;
    ipPanel.add(waveFormPort = new JTextField(6), c);
    waveFormPort.setEditable(false);
    
    c.gridy++;
    c.gridx = 0;
    ipPanel.add(waveFormBandWidth = new JLabel("wave Form Bandwidth", JLabel.RIGHT), c);
    c.gridx++;
    ipPanel.add(jcWaveFormBandWidth = new JComboBox(IcListenParams.WAVE_FORM_BANDWIDRH), c);
    jcWaveFormBandWidth.addItemListener(new ItemHandler());
    c.gridx++;
    ipPanel.add(gives_SampleRate = new JLabel("Gives a sample rate of:"), c);
    c.gridx++;
    ipPanel.add(sampleRate = new JTextField(8), c);
    sampleRate.setText("" + icListenParams.Sample_Rate);
    acquisitionDialog.setSampleRate(Float.parseFloat(sampleRate.getText()));
    sampleRate.setToolTipText("Default sample rate is 1000.");
    sampleRate.setEditable(false);
    c.gridx++;
    ipPanel.add(hz = new JLabel("hz"), c);
    c.gridy++;
    c.gridx = 0;

    ipPanel.add(connectionToDevice = new JTextField(20), c);
    connectionToDevice.setText(icListenParams.connectionStatus);
    connectionToDevice.setEditable(false);
    c.gridx++;

    //initiazing the socket connections to the icListen devices.
    ipPanel.add(connect = new JButton("connect"), c);
    // establish connections with both the command socket and stream socket.
    connect.addActionListener(new ActionListener() {
      @Override
      //invalid field for timeout values
      public void actionPerformed(ActionEvent arg0) {
        if (!(timeout.getText().matches("^[0-9]*$"))) {
          JOptionPane.showMessageDialog(null, "Please input a integer value for the time out!");
          return;
        }

        String text = ipAddress.getText();
        System.out.println("The IP address is " + text);
        icListenParams.serverAddress = text;
        SocketAddress socketAddress = new InetSocketAddress(icListenParams.serverAddress,
            IcListenParams.commandControlPort);
        icListenParams.commandSocket = new Socket();

        SocketAddress streamSocketAddress = new InetSocketAddress(icListenParams.serverAddress,
            IcListenParams.waveFormPort);
        icListenParams.streamSocket = new Socket();
        try {
          icListenParams.commandSocket.connect(socketAddress, IcListenParams.TIMEOUT);
          // IcListenParams.streamSocket.connect(streamSocketAddress,IcListenParams.TIMEOUT);
          icListenParams.connect = true;
        } catch (Exception e) {
          int a = JOptionPane.showConfirmDialog(null,
              "Unable to establish connection with the IP address, please try again later or modify the address",
              "Connection timeout", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
          e.printStackTrace();
          System.out.println("invalid Socket Address.");
        }
        
        
        

        if (icListenParams.connect) {
          try {

            connect.setEnabled(false);
            timeout.setEnabled(false);
            icListenParams.outToServer = icListenParams.commandSocket.getOutputStream();
            icListenParams.OutStream = new DataOutputStream(icListenParams.outToServer);
            icListenParams.inFromServer = icListenParams.commandSocket.getInputStream();
            icListenParams.inStream = new DataInputStream(icListenParams.inFromServer);

            icListenParams.OutStream.write(icListenParams.BASIC_COMMAND);
            icListenParams.inStream.read(icListenParams.buffer);

            System.out.println("The device is " + icListenParams.buffer[32]);
            String device_Type = deviceType(icListenParams.buffer[32]);
            int serial_Num = (int) (icListenParams.buffer[4] & 0xFF
                | (icListenParams.buffer[5] & 0xFF) << 8);
            icListenParams.connectionStatus = "connected to " + device_Type + " " + serial_Num;
            icListenParams.commandSocket.close();

            socketAddress = new InetSocketAddress(icListenParams.serverAddress,
                IcListenParams.commandControlPort);
            icListenParams.commandSocket = new Socket();
            icListenParams.commandSocket.connect(socketAddress, icListenParams.TIMEOUT);
            icListenParams.outToServer = icListenParams.commandSocket.getOutputStream();
            icListenParams.OutStream = new DataOutputStream(icListenParams.outToServer);
            icListenParams.inFromServer = icListenParams.commandSocket.getInputStream();
            icListenParams.inStream = new DataInputStream(icListenParams.inFromServer);

            disconnect.setEnabled(true);
            jcWaveFormBandWidth.setEnabled(false);

            icListenParams.OutStream.write(jobSetup(), 0, payloadLength1);
            byte[] buffer = new byte[icListenParams.MAX_MESSAGE_SIZE];
            try {
              icListenParams.inStream.read(buffer);
            } catch (Exception e) {
              e.printStackTrace();
            }
            System.out.println("????dsafdfs");
            icListenParams.streamSocket.connect(streamSocketAddress, icListenParams.TIMEOUT);
            icListenParams.stroutToServer = icListenParams.streamSocket.getOutputStream();
            icListenParams.strOutStream = new DataOutputStream(icListenParams.stroutToServer);
            icListenParams.strinFromServer = icListenParams.streamSocket.getInputStream();
            icListenParams.strinStream = new DataInputStream(icListenParams.strinFromServer);
            connectionToDevice.setText(icListenParams.connectionStatus);
            acquisitionDialog.setSampleRate(icListenParams.Sample_Rate);
            
          } catch (IOException e) {

            e.printStackTrace();
            disconnect.setEnabled(true);
          }
        }
      }

    });

    c.gridx++;
    ipPanel.add(disconnect = new JButton("disconnect"), c);
    disconnect.setEnabled(false);

    // while disconnect button clicked, close the connections with the icListen
    disconnect.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        try {
          icListenParams.connectionStatus = "No icListen Device connected";

          connectionToDevice.setText(icListenParams.connectionStatus);
          icListenParams.commandSocket.close();
          icListenParams.streamSocket.close();
          icListenParams.connect = false;
        } catch (IOException e) {
          e.printStackTrace();
        }
        disconnect.setEnabled(false);
        timeout.setEnabled(true);
        connect.setEnabled(true);
        jcWaveFormBandWidth.setEnabled(true);
      }

    });

    this.add(ipPanel);      
  }

  private class ItemHandler implements ItemListener {
    /**
     * returns the proper sample rate based on the values from the dropdown menus
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
      if (event.getSource() == jcWaveFormBandWidth) {
        if (jcWaveFormBandWidth.getSelectedItem().equals("400 Hz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[0]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[0]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("800 Hz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[1]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[1]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("1600 Hz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[2]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[2]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("3200 Hz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[3]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[3]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("6400 Hz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[4]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[4]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("12.8 kHz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[5]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[5]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("25.6 kHz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[6]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[6]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("51.2 kHz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[7]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[7]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("102.4 kHz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[8]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[8]);
        } else if (jcWaveFormBandWidth.getSelectedItem().equals("204.8 kHz")) {
          sampleRate.setText(IcListenParams.SAMPLE_RATE[9]);
          icListenParams.Sample_Rate = Integer.parseInt(icListenParams.SAMPLE_RATE[9]);
        }
        acquisitionDialog.setSampleRate(icListenParams.Sample_Rate);

      }
    }
  }

  
  //Set up the job
  public byte[] jobSetup() {

    // TODO pass this the index to send the setup to
    /* Get the payload length */
    char[] payloadLength = new char[] { 0xFF & JOB_SETUP_PAYLOAD_LEN, (0xFF & (JOB_SETUP_PAYLOAD_LEN >> 8)) };

    /* Create the payload array */
    char[] payload = new char[] {
        /* Enter the type of job setup */
        (char) (JOB_SETUP_TYPE & 0xFF), (char) ((JOB_SETUP_TYPE >> 8) & 0xFF),

        /* The number of tags being used */
        (NUMBER_OF_TAGS & 0xFF), ((NUMBER_OF_TAGS >> 8) & 0xFF),

        /* Set up the sample rate using its tag and the sample rate from the settings */
        (SAMPLE_RATE_TAG & 0xFF), ((SAMPLE_RATE_TAG >> 8) & 0xFF), (TAG_VAL_LEN & 0xFF),
        ((TAG_VAL_LEN & 0xFF) >> 8), (char) (icListenParams.Sample_Rate & 0xFF),
        (char) ((icListenParams.Sample_Rate >> 8) & 0xFF), (char) ((icListenParams.Sample_Rate >> 16) & 0xFF),
        (char) ((icListenParams.Sample_Rate >> 24) & 0xFF),

        /* Set up the bit depth using its tag, set to 24 bit */
        (BIT_DEPTH_TAG & 0xFF), ((BIT_DEPTH_TAG >> 8) & 0xFF), (TAG_VAL_LEN & 0xFF),
        ((TAG_VAL_LEN & 0xFF) >> 8), (char) (BIT_DEPTH_VALUE & 0xFF), (char) ((BIT_DEPTH_VALUE >> 8) & 0xFF),
        (char) ((BIT_DEPTH_VALUE >> 16) & 0xFF), (char) ((BIT_DEPTH_VALUE >> 24) & 0xFF),

        /* Set up the digital gain using its tag, set to no gain */
        (GAIN_TAG & 0xFF), ((GAIN_TAG >> 8) & 0xFF), (TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN & 0xFF) >> 8),
        (char) (GAIN_VALUE & 0xFF), (char) ((GAIN_VALUE >> 8) & 0xFF), (char) ((GAIN_VALUE >> 16) & 0xFF),
        (char) ((GAIN_VALUE >> 24) & 0xFF),

        /* Set up the endianness using its tag, set to Big endian */
        (ENDIAN_TAG & 0xFF), ((ENDIAN_TAG >> 8) & 0xFF), (TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN >> 8) & 0xFF),
        (ENDIAN_VALUE & 0xFF), ((ENDIAN_VALUE >> 8) & 0xFF), ((ENDIAN_VALUE >> 16) & 0xFF),
        ((ENDIAN_VALUE >> 24) & 0xFF),

        /* Disable logging using its tag */
        (LOGGING_MODE_TAG & 0xFF), ((LOGGING_MODE_TAG >> 8) & 0xFF), (TAG_VAL_LEN & 0xFF),
        ((TAG_VAL_LEN >> 8) & 0xFF), (LOGGING_MODE_VAL & 0xFF), ((LOGGING_MODE_VAL >> 8) & 0xFF),
        ((LOGGING_MODE_VAL >> 16) & 0xFF), ((LOGGING_MODE_VAL >> 24) & 0xFF), };

    commandMessage jobSetup = new commandMessage(SYNC_BYTE, JOB_SETUP_COMMAND, payloadLength, payload);

    byte[] buffer = sendCommandMessage(jobSetup);
    return buffer;

  }

  //put the command message into a format
  public byte[] sendCommandMessage(commandMessage msg) {
    int payloadLength;

    int bufferIndex;

    payloadLength = (msg.payloadLength[0] & 0xFF) | (msg.payloadLength[1] & 0xFF << 8);

    byte[] buffer = new byte[MIN_MESSAGE_LEN + payloadLength];
    payloadLength1 = MIN_MESSAGE_LEN + payloadLength;

    buffer[0] = (byte) (msg.sync & 0xFF);
    buffer[1] = (byte) (msg.messageType & 0xFF);
    buffer[2] = (byte) (msg.payloadLength[0] & 0xFF);
    buffer[3] = (byte) (msg.payloadLength[1] & 0xFF);

    for (bufferIndex = 4; bufferIndex < (payloadLength + 4); bufferIndex++) {
      buffer[bufferIndex] = (byte) (msg.payload[bufferIndex - 4] & 0xFF);
    }

    buffer[bufferIndex] = (byte) (msg.CRC[0] & 0xFF);
    buffer[bufferIndex + 1] = (byte) (msg.CRC[1] & 0xFF);

    return buffer;
  }

  /**
   * 
   * @param num32
   * @return the device type based on returned information
   */
  public String deviceType(byte num32) {
    String device_Type = "";
    switch (num32) {
    case 0x01: {
      device_Type = "icTalk LF";
      break;
    }
    case 0x02: {
      device_Type = "icListen LF";
      break;
    }
    case 0x03: {
      device_Type = "StandALone Guest Sensor";
      break;
    }
    case 0x04: {
      device_Type = "icTalk HF";
      break;
    }

    case 0x05: {
      device_Type = "icListen HF";
      break;
    }
    case 0x07: {
      device_Type = "icListen AF";
      break;
    }
    case 0x41: {
      device_Type = " icListen MF";
      break;
    }

    default: {
      device_Type = "undetermined";
    }
    }

    return device_Type;

  }

  /**
   * 
   * @param icListenParams
   */
  public void setParams(IcListenParams icListenParams) {
    ipAddress.setText(icListenParams.serverAddress);

    tcpPort.setText(String.format("%d", icListenParams.commandControlPort));

    waveFormPort.setText(String.format("%d", icListenParams.waveFormPort));
    icListenParams.connect = false;
    acquisitionDialog.setChannels(1);
    acquisitionDialog.setVPeak2Peak(icListenDaq.ICLISTEN_PEAKTOPEARKV);
    acquisitionDialog.setPreampGain(0);

  }

  /**
   * This method might not be very useful in terms of the functionality, but might
   * as well keep it.
   * 
   * @param icListenParams
   * @return true
   */
  public boolean getParams(IcListenParams icListenParams) {
    icListenParams.serverAddress = ipAddress.getText();
    // if recieved field is empty
    if (icListenParams.serverAddress.equals(""))
      JOptionPane.showMessageDialog(null, "The field cant be null.");
    try {
      icListenParams.commandControlPort = Integer.valueOf(tcpPort.getText());
    } catch (NumberFormatException e) {
      return acquisitionDialog.showWarning("Invalid Network Port");
    }
    return true;
  }

}
