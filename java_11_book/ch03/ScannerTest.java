/*
 *  test java.util.Scanner
 *  shane 
 *  2020_12_20
 */
package java_11_book.ch03;
import java.util.Scanner;

public class ScannerTest{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("scanner object created");
        String str1;
        str1 = scanner.nextLine();
        int a = 25;
        String str2 = new String(String.valueOf(a));
        System.out.println("str1: " + str1);
        System.out.println("str2: " + str2);
    }
}