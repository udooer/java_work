package SOCKET;
/* 
 *  try socket lib in java
 *  Shane
 *  2021_01_06
 */
import java.io.*;
import java.net.*;

public class MyServer{
    public static void main(String[] args){
        try{
            ServerSocket ss = new ServerSocket(6666);
            System.out.println("create a server at localhost:6666");
            Socket s = ss.accept();
            System.out.println("get input stream.");
            DataInputStream dis = new DataInputStream(s.getInputStream());
            System.out.println("read the utf data");
            String str = (String)dis.readUTF();
            System.out.println("message=" + str);
            ss.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}