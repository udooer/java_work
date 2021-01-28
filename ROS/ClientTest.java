package Test;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;

public class ClientTest{
    public WebSocketClient m_ws;
    ClientTest(){
        try{
            m_ws = new WebSocketClient(new URI("ws://0.0.0.0:9090"), new Draft_6455()){
                @Override
                public void onMessage( String message ) {
                    System.out.println( "on message" );
                    JSONObject obj = new JSONObject(message);
                    JSONObject msg = (JSONObject)obj.get("msg");
                    JSONArray ch1 = msg.getJSONArray("data_ch1");
                    double[] data_double = new double[ch1.length()];
                    for(int i=0; i<ch1.length(); i++){
                        data_double[i] = ch1.getDouble(i);
                    }
                    System.out.println(ch1.length());
                }

                @Override
                public void onOpen( ServerHandshake handshake ) {
                    System.out.println( "opened connection" );
                }

                @Override
                public void onClose( int code, String reason, boolean remote ) {
                    System.out.println( "closed connection" );
                }

                @Override
                public void onError( Exception ex ) {
                    ex.printStackTrace();
                }
            };
        }catch(URISyntaxException e){
            System.out.println(e);
        }
    }
    public static void main(String[] args){
        ClientTest test = new ClientTest();
        test.m_ws.connect();
        try{
            Thread.sleep(2000);
        }catch(Exception e){
            System.out.println(e);
        }
        JSONObject obj = new JSONObject();
        obj.put("op", "subscribe");
        obj.put("topic", "/get_sound_data_for2i2/hydrophone_data");
        obj.put("type", "ntu_msgs/HydrophoneData");
        String message = obj.toString();
        //send message
        test.m_ws.send(message);
        System.out.println("subscribed message sended");
    }
}