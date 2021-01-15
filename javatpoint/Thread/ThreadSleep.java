package Thread;

public class ThreadSleep extends Thread{
    public void run(){
        String name = this.getName();
        for(int i=0;i<5;i++){
            System.out.println(name + ":" + i);
            // try{this.sleep(500);
            // }catch(Exception e){}
        }
    }
    public static void main(String[] args){
        ThreadSleep t1 = new ThreadSleep();
        ThreadSleep t2 = new ThreadSleep();
        t1.setName("t1");
        t2.setName("t2");
        t1.start();
        t2.start();
    }
}