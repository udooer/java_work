/*
 *  primitive variable test 
 *  shane 
 *  2020_12_20
 */
package java_11_book.ch03;
import java.lang.*;

public class VariableTest{
    long count = 10000000000L;
    float money = 2.6f;
    public static void main(String[] args){
        System.out.println("Test the bound of primitive variable");
        System.out.println("Byte 	bound is from " + Byte.MAX_VALUE + " to " + Byte.MIN_VALUE);
        System.out.println("Int 	bound is from " + Integer.MAX_VALUE + " to " + Integer.MIN_VALUE);
        System.out.println("Float 	bound is from " + Float.MAX_VALUE + " to " + Float.MIN_VALUE);
    }
}