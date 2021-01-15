package Thread;

public class ThreadTest implements Runnable{
	public void run(){
		System.out.println("thread is running");
	}
	public static void main(String[] args){
		ThreadTest test = new ThreadTest();
		Thread t = new Thread(test);
		t.start();
		try{Thread.sleep(1000);
		}catch(Exception e){
		}
		System.out.println("out of thread run function");
	}
}