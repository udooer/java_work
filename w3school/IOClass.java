/*
 *  test inner outter class 
 *  shane 
 *  2020_12_21
 */

package w3school;

class OutterClass{
    String x = "attibute of outter class";
    public class InnerClass{
        String x = "attribute of inner class";
        public String getX(){
            return x;
        }
        public String getThisX(){
            return this.x;
        }
    }
}

public class IOClass{
    public static void main(String[] args){
        OutterClass out_class = new OutterClass();
        OutterClass.InnerClass in_class = out_class.new InnerClass();
        String get_x = in_class.getX();
        String get_this_x = in_class.getThisX();
        System.out.println("get_x is : " + get_x);
        System.out.println("get_this_x is :" + get_this_x);
    }
}