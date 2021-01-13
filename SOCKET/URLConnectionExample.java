package SOCKET;

import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedInputStream;

public class URLConnectionExample{
	public static void main(String[] args){
		try{
			URL url = new URL("https://www.javatpoint.com/URLConnection-class");
			URLConnection urlcon = url.openConnection();
			BufferedInputStream bin = new BufferedInputStream(urlcon.getInputStream());
			int i;
			while((i=bin.read())!=-1){
				System.out.print((char)i);
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}
}