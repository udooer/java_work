/* 
 *  test substring method in String
 *  Shane
 *  2020_12_28
 */
package String;
public class SubstringTest{
	public static void main(String[] args){
		String s = "c++ is selected";
		String sub_s = s.substring(0,3);
		System.out.println("sub_s is :" + sub_s);
		if(sub_s.equals("c++")){
			System.out.println("the item is same.");
		}
		if("c++"=="c++"){
			System.out.println("\"c++\"==\"c++\" is true");
		}
		else
			System.out.println("\"c++\"==\"c++\" is false");
	}
}