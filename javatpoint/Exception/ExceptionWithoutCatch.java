package Exception;

public class ExceptionWithoutCatch{
	public static void main(String[] args){
		System.out.println("in the main function");
		try{int i = 2/0;
		}finally{
			// int i = 2/0;
			System.out.println("important stuff processing!!!");
		}
		System.out.println("the rest of the code.");
	}
}