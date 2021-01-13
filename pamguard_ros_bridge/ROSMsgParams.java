package pamguard_ros_bridge;
/*
 *  create a class to store the global varible 
 *  which is user in this package.
 */

// import java_websocket lib
import org.java_websocket.client.WebSocketClient;
public class ROSMsgParams{
    public WebSocketClient m_ws;
    public boolean m_status = false;
}