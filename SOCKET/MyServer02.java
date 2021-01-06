package SOCKET;

import java.io.*;
import java.net.*;

public class MyServer02{
    public static void main(String[] args) throws Exception{
        ServerSocket ss = new ServerSocket(6666);
        System.out.println("create a socket as localhost:6666");
        Socket s = ss.accept();

        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String str="", str2="";
        int count = 0;
        while(!str2.equals("stop")){
            str2 = br.readLine();
            if(str2.length()==0){
                System.out.println("get no command");
            }
            str = din.readUTF();
            System.out.println("client says:" + str);
            dout.writeUTF("hello client" + count);
            count++;
            dout.flush();
        }
        din.close();
        dout.close();
        s.close();
        ss.close();
    }
}