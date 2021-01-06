package SOCKET;
/* 
 *  try socket lib in java
 *  Shane
 *  2021_01_06
 */
import java.net.*;
import java.io.*;
public class MyClient02{
	public static void main(String[] args) throws Exception{
		Socket s = new Socket("localhost", 6666);
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

			dout.writeUTF("hello server" + count);
			count++;
			dout.flush();

			str = din.readUTF();
			System.out.println("Server says" + str);
		}
		dout.close();
		din.close();
		s.close();
	}
}