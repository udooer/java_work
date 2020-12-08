class MethodOverload{
    static int add(int x, int y){
        return x+y;
    }
    static int add(int x, int y, int z){
        return x+y+z;
    }
    public static void main(String[] args){
        int x=5,y=6,z=7;
        System.out.print("int add(int x, int y): ");
        System.out.println(add(x,y)+"\n\n");
        System.out.print("int add(int x, int y, int z): ");
        System.out.println(add(x,y,z)+"\n\n");
    }
}