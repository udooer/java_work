package IO;
/* 
 *  try java IO lib 
 *  Shane
 *  2021_01_07
 */

import java.io.FileOutputStream;
public class FileOutputStreamExample{
    public static void main(String[] args){
        try{
            FileOutputStream f = new FileOutputStream("/home/yong/java_work/IO/txt/test.txt");
            System.out.println("start to write");
            f.write(65);
            String s = "\nmy name is Shane";
            byte[] b = s.getBytes();
            f.write(b,0,16);
            f.close();
            System.out.println("finish writing");
        }catch(Exception e){
            System.out.println(e);
        }
    }
} 