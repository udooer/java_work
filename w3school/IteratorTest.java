package w3school;
/* 
 *  test Iterator lib 
 *  Shane
 *  2020_12_24
 */
import java.util.ArrayList;
import java.util.Iterator;
public class IteratorTest{
    public static void main(String[] args){
        ArrayList<Integer> test = new ArrayList<Integer>();
        test.add(5);
        test.add(12);
        test.add(7);
        test.add(-5);
        test.add(0);
        Iterator<Integer> it = test.iterator();
        System.out.println("loop through with iterator");
        while(it.hasNext()){
            System.out.println(it.next());
        }
        System.out.println("After filtering the data:");
        it = test.iterator();
        while(it.hasNext()){
            if(it.next()<10){
                it.remove();
            }
        }
        System.out.println(test);
    }
}