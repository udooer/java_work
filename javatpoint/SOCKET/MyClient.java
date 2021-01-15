package SOCKET;
/*  
 *  try socket lib in java
 *  Shane
 *  2021_01_06
 */
import java.io.*;
import java.net.*;

public class MyClient{
    public static void main(String[] args){
        try{
            Socket s = new Socket("localhost", 6666);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            dout.writeUTF("hello server");
            dout.flush();
            dout.close();
            s.close();
        }catch(Exception e){
            System.out.println(e);
        }
    }
}