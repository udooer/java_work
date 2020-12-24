package w3school;
/*
 *  test HashMap structure 
 *  Shane
 *  2020_12_24
 */

import java.util.HashMap;

public class HashMapTest{
    public static void main(String[] args){
        HashMap<String, String> test = new HashMap<String, String>();
        test.put("shane", "smart");
        test.put("shawn", "tall");
        test.put("mireille", "pretty");
        System.out.println("HashMap created, size is: " + test.size());
        System.out.println("test HashMap: ");
        System.out.println(test);
        System.out.println("\nloop through key set");
        for(String i:test.keySet()){
            System.out.println(i);
        }
        System.out.println("\nloop through value");
        for(String i:test.values()){
            System.out.println(i);
        }
    }
}