package Exception;

public class ExceptionWithCatch{
	public static void main(String[] args){
		System.out.println("in the main function");
		try{
			int i = 2/0;
		}catch(Exception e){
			System.out.println("catch the exception:\n" + e);
		}finally{
			System.out.println("important stuff processing!!!");
		}
		System.out.println("the rest of the code.");
	}
}