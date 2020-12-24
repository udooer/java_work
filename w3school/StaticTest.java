/*
 *  test static and public modifier
 *  shane
 *  2020_12_21
 */ 

package w3school;

public class StaticTest{
    static void myStaticFunction(){
        System.out.println("static function called");
    }
    public void myPublicFunction(){
        System.out.println("public function called");
    }
    public static void main(String[] args){
        System.out.println("directly call static function");
        myStaticFunction();
        System.out.println("\n\nobject of StaticTest created");
        StaticTest test = new StaticTest();
        System.out.println("call public function by object");
        test.myPublicFunction();
        System.out.println("call static function by object");
        test.myStaticFunction();
    }
}