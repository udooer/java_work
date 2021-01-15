package IO;
/*
 *  try java IO lib
 *  Shane
 *  2021_01_07
 */
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
public class BufferedOutputStreamExample{
	public static void main(String[] args) throws Exception{
		FileOutputStream fout = new FileOutputStream("/home/yong/java_work/IO/txt/test01.txt");
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		String s = "I love java.\nMy name is Shane.";
		byte[] b = s.getBytes();
		System.out.println("start to write");
		bout.write(b);
		bout.flush();
		bout.close();
		fout.close();
		System.out.println("finish writing");	
	}
}