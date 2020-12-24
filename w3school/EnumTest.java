/*
 *  test the enum structure
 *  Shane
 *  2020_12_21
 */ 

package w3school;

enum Level{
	HIGH,
	MEDIUM,
	LOW
}

public class EnumTest{
	public static void main(String [] args){
		Level my_level = Level.HIGH;
		System.out.println("my level is : " + my_level);
		System.out.println("loop through level enum");
		for(Level i:my_level.values()){
			System.out.println(i);
		}
	}
}