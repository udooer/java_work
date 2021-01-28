package pamguard_ros_bridge;
/*
 *  create a class to store the global varible 
 *  which is user in this package.
 */
import java.util.concurrent.BlockingQueue;

// import java_websocket lib
import org.java_websocket.client.WebSocketClient;

//import pamguard lib
import Acquisition.AudioDataQueue;

public class ROSMsgParams{
    public WebSocketClient m_ws;
    public volatile boolean m_status = false;
    public AudioDataQueue m_audioDataQueue_ch1;
    public BlockingQueue<double[]> m_msgList;
}