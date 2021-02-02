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
import java.util.concurrent.*;

public class ClientWithMultiThread{
    public WebSocketClient m_ws;
    private BlockingQueue<double[]> msg_list;
    ClientWithMultiThread(BlockingQueue<double[]> msg_list){
        this.msg_list = msg_list;
        try{
            m_ws = new WebSocketClient(new URI("ws://0.0.0.0:9090"), new Draft_6455()){
                @Override
                public void onMessage(String message){
                    System.out.println("*****ON MESSAGE START*****");
                    JSONObject obj = new JSONObject(message);
                    JSONObject msg = (JSONObject)obj.get("msg");
                    JSONArray ch1 = msg.getJSONArray("data_ch1");
                    double[] data_double = new double[ch1.length()];
                    for(int i=0; i<ch1.length(); i++){
                        data_double[i] = ch1.getDouble(i);
                    }
                    msg_list.add(data_double);
                    System.out.println("data length:" + data_double.length);
                    System.out.println("msg list length:" + msg_list.size());
                    System.out.println("*****ON MESSAGE END*****");
                }

                @Override
                public void onOpen(ServerHandshake handshake){
                    System.out.println("opened connection");
                }

                @Override
                public void onClose(int code, String reason, boolean remote){
                    System.out.println("closed connection");
                }

                @Override
                public void onError(Exception ex){
                    ex.printStackTrace();
                }
            };
        }catch(URISyntaxException e){
            System.out.println(e);
        }
    }

    public static void main(String[] args){
        BlockingQueue<double[]> msg_list = new LinkedBlockingQueue<double[]>();
        ClientWithMultiThread test = new ClientWithMultiThread(msg_list);
        test.m_ws.connect();
        try{
            Thread.sleep(2000);
        }catch(InterruptedException ie){
            return;
        }
        catch(Exception e){
            System.out.println("exception happen in main function");
        }

        JSONObject obj = new JSONObject();
        obj.put("op", "subscribe");
        obj.put("topic", "/get_sound_data_for2i2/hydrophone_data");
        obj.put("type", "ntu_msgs/HydrophoneData");
        String message = obj.toString();
        //send message
        test.m_ws.send(message);
        System.out.println("subscribed message sended");
        try{
            Thread.sleep(2000);
        }catch(InterruptedException ie){
            return;
        }
        catch(Exception e){
            System.out.println("exception happen in main function");
        }
        DataStreamThread data_stread = new DataStreamThread(msg_list);
        new Thread(data_stread).start();
    }
}

class DataStreamThread implements Runnable{
        private BlockingQueue<double[]> msg_list;
        
        DataStreamThread(BlockingQueue<double[]> msg_list){
            this.msg_list = msg_list;
        }
        public void run(){
            double[] buffer = new double[9600];
            while(true){
                try{
                    System.out.println("+++++DATA STREAM THREAD HEAD+++++");
                    buffer = msg_list.take();
                    System.out.println("data length" + buffer.length);
                    System.out.println("msg list length:" + msg_list.size());
                    System.out.println("+++++DATA STREAM THREAD END+++++");
                }catch(InterruptedException ie){
                    System.out.println("InterruptedException!!!");
                    break;
                }
            }
        }
    }