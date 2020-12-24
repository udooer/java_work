/*
 *  test the ArrayList structure
 *  Shane
 *  2020_12_21
 */

package w3school;

import java.util.ArrayList;
import java.util.Collections;

public class ArrayListTest{
    public static void main(String[] args){
        ArrayList<Integer> my_array = new ArrayList<Integer>(2);
        System.out.println("my_array is created");
        System.out.println("my_array size is: " + my_array.size());
        my_array.add(1);
        my_array.add(6);
        my_array.add(3);
        my_array.add(10);
        my_array.add(-2);
        for(int i:my_array){
            System.out.println(i);
        }
        System.out.println("after insert number:");
        System.out.println("my_array size is: " + my_array.size());
        Collections.sort(my_array);
        System.out.println("\nafter sorting: ");
        for(int i:my_array){
            System.out.println(i);
        }
    }
}