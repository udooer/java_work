/*
 *  test array in java
 *  Shane
 *  2020_12_22
 */

package ch05;
import java.util.Arrays;

public class ArrayTest{
    public static void main(String[] args){
        int test[] = new int[]{5,9,-2,7,1};
        System.out.println("Array created, length is :" + test.length);
        for(int i:test){
            System.out.print(i + " ");
        }
        System.out.println("\nAfter Array sorted");
        Arrays.sort(test);
        for(int i:test){
            System.out.print(i + " ");
        }
        System.out.println();
    }
}