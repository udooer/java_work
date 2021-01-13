package IO;
/*
 *  try io lib in java 
 *  Shane
 *  2021_01_07
 */

import java.io.FileInputStream;

public class FileInputStreamExample{
	public static void main(String[] args){
		try{
			FileInputStream fin = new FileInputStream("/home/yong/java_work/IO/txt/test.txt");
			int i;
			System.out.println("start to read file");
			while((i=fin.read())!=-1){
				System.out.print((char)i);
			}
			fin.close();
			System.out.println("\nfinish reading");
		}catch(Exception e){
			System.out.println(e);
		}		
	}
}