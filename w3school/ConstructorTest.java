/*
 *  test java class constructor
 *  shane 
 *  2020_12_21
 */

package w3school;

public class ConstructorTest{
    private ConstructorTest(){
        System.out.println("initalize the ConstructorTest object");
    }
    public static void main(String[] args){
        ConstructorTest test = new ConstructorTest();
    }
}