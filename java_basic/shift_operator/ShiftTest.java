public class ShiftTest{
	public static void main(String[] args){
		int test=1;
		for(int i=1;i<4;i++){
			test = test << i;
			System.out.println("1 << " + i + ":" + test);
			test = 1;
		}
	}
}