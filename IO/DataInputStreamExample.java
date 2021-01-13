package IO;
/*
 *  try java IO lib 
 *  Shane
 *  2021_01_07
 */
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;

public class DataInputStreamExample{
    public static void main(String[] args) throws Exception{
        FileInputStream fin = new FileInputStream("/home/yong/java_work/IO/txt/test03.txt");
        DataInputStream din = new DataInputStream(fin);
        int count = fin.available();
        System.out.println("total bytes are " + count + " bytes");
        // int i = din.readInt();
        // System.out.println(i);
        String s = din.readUTF();
        System.out.println(s);
    }
}