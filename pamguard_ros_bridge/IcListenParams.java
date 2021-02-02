package icListenPluginDaqBETA3_4;

/**
 * @author: Yangfan Chen
 * Functionality: This class stores all the important parameters
 * that will be used for connecting with the icListen devices, and use
 * those data for spectrum display by Pamguard.
 */
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import Acquisition.AudioDataQueue;

public class IcListenParams implements Cloneable {

  public static final long serialVersionUID = 1L;
  /* The various variables needed for the icListenPanel */
  protected volatile boolean stopCature;
  protected volatile boolean captureRunning;

  // this default server address is used for testing
  protected String serverAddress = "172.16.0.107";

  // put your data into the queue and Pamguard will process it  in the background.
  protected AudioDataQueue audioDataQueue;

  // the maximum length of the message between your buffer and icListen Device.
  protected static final int MAX_MESSAGE_SIZE = 6706;

  // two ports that will be used and necessary for data streams.
  protected static int commandControlPort = 50000;
  protected static int waveFormPort = 51678;
  
  //default sample rate and time out used for testing purposes.
  protected static int sampleRate = 1000;
  protected static int TIMEOUT = 5000;

  //the 3 variables below are used to display the connection status.
  protected volatile boolean connect = false;
  protected String connectionStatus = "No icListen Device connected";
  protected volatile boolean reconnect = false;// reconnect is not used at all, should have been deleted.
  // basic enquire device command used to retrieve the type, status, and firmware
  // version
  // of the connected device
  // TODO bad name for enquire_command message, 
  protected static byte[] BASIC_COMMAND = { 42, 69, 0, 0, 25, -51, 0, 0, 0 };
  // once the connection sets up, we can store the returned message into the
  // buffer.
  protected  byte[] buffer = new byte[100];

  // will be used for the command and control port to retrieve the serial number
  // and device type
  protected Socket commandSocket;
  protected OutputStream outToServer;
  protected DataOutput OutStream;
  protected InputStream inFromServer;
  protected DataInputStream inStream;

  
  //the variable and constants below are associated with different sample rate.
  protected  int Sample_Rate = 1000;

  protected static final String[] WAVE_FORM_BANDWIDRH = { "400 Hz", "800 Hz", "1600 Hz", "3200 Hz", "6400 Hz",
      "12.8 kHz", "25.6 kHz", "51.2 kHz", "102.4 kHz", "204.8 kHz" };

  protected static final String[] SAMPLE_RATE = { "1000", "2000", "4000", "8000", "16000", "32000", "64000", "128000",
      "256000", "512000" };

  
  //the sockets and IOs are used for stream sockets.
  protected Socket streamSocket;
  protected OutputStream stroutToServer;
  protected DataOutput strOutStream;
  protected InputStream strinFromServer;
  protected DataInputStream strinStream;

  // Stream channel Message the streaming channel message have the common format
  // below
  protected static final char NOTIFY = 0x30;// '0' Byte Offset Field #Bytes
  protected static final char DATA = 0x31;// '1' 0 Message Type 1
  protected static final char EVENT_HEADER = 0x32;// '2' 1 sync(0x2A) 1
  protected static final char START_STREAM = 0x33;// '3' 2 Payload Length 2
  protected static final char STOP_STREAM = 0x34;// '4' 4 Payload n
  protected static final char OTHER_SENSOR = 0x35;// '5'

  /**
   * Event Header: sent up to once per second, before any data is sent for that
   * second. this message type is only sent from waveform or spectrum data
   * channels.
   * 
   * Byte Offset Fields #Bytes 0 type('2')0x32 1 1 Sync(0x2A) 1 2 Payload Length 2
   * 4 Event Key Chunk 16 //time keeping and data synchronized 20 Device Info
   * Chunk k //hardware specific implementation for the instrument 20+k Status
   * Chunk p //extra status information 20+k+p Setup Chunk i //setup acquired the
   * following info 20+k+p+i Amplitude Scal- j //scale the data to real world unit
   * ing Chunk
   */

  /**
   * Message Chunk Byte Offset Field #Bytes 0 Chunk Type 1 //type of chunk being
   * transmitted 1 Version 1 //version of chunk payload follows 2 Chunk Size 2 //#
   * of bytes in the payload 4 Payload n //vary in length and content, based on
   * the type of chunk being transmitted
   */
  protected static final char SYNC = 0x2A;
  // the payload length took 2 bytes

  // message chunk
  protected static final char EVENT_KEY_CHUNK = 0x41; // contains time/sequence number,used to pair data with header
  protected static final char DATA_CHUNK = 0x42; // contains waveform or spectrum data
  protected static final char STATUS_CHUNK = 0x43; // contains various sensor data, included with data header
  protected static final char DEVICE_INFO_CHUNK = 0x44; // contains with hardware or firmware details
  protected static final char WAVE_SETUP_CHUNK = 0x45; // contains the waveform data setup
  protected static final char FFT_SETUP_CHUNK = 0x46; // contains the spectrum data setup
  protected static final char SCALING_CHUNK = 0x47; // contains the details for scaling data
  protected static final char TEMP_HUMIDTY_CHUNK = 0x48; // contains temperature and humidity data
  protected static final char HEADING_CHUNK = 0x49; // contains accelerometer and magnetmeter data
  protected static final char TIME_SYNC_CHUNK = 0x50; // contains time sync and pps details.
  protected static final char BATTERY_CHUNK = 0x51; // contains battery details and status
  protected static final char TRIGGER_STATUS_CHUNK = 0x52;// contains the status of all epoch triggers

  // can clone the parameters.
  @Override
  protected IcListenParams clone() {
    try {
      return (IcListenParams) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }
}
