package IO;
/*
 *  try java IO lib
 *  Shane
 *  2021_01_07
 */
import java.io.FileOutputStream;
import java.io.DataOutputStream;

public class DataOutputStreamExample{
    public static void main(String[] args) throws Exception{
        FileOutputStream fout = new FileOutputStream("/home/yong/java_work/IO/txt/test03.txt");
        DataOutputStream dout = new DataOutputStream(fout);
        System.out.println("start writing");
        // int i = 1999;
        // dout.writeInt(i);
        String s = "my name is shane";
        dout.writeUTF(s);
        dout.flush();
        dout.close();
        fout.close();
        System.out.println("finish writing");
    }
}