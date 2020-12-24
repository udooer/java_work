package w3school;
/*
 *  test Map Set Collection HashMap structure 
 *  Shane 
 *  2020_12_24
 */

import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class AdvancedMapTest{
    public static void main(String[] args){
        Map<String, Integer> test = new HashMap<String, Integer>();
        test.put("Shane", 24);
        test.put("Logan", 25);
        test.put("Morris", 28);
        test.put("Venassa", 17);
        test.put("Sam", 35);
        System.out.println(test);
        Set<String> key_set = test.keySet();
        Collection<Integer> value = test.values();

        Iterator<String> it_s = key_set.iterator();
        Iterator<Integer> it_i = value.iterator();

        while(it_s.hasNext() && it_i.hasNext()){
            System.out.println("key is :" + it_s.next() + ", value is :" + it_i.next());
        }
    }
}