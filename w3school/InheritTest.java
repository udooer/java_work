/*
 *  test inherit and polymorphism
 *  Shane
 *  2020_12_21
 */

package w3school;

class Animal{
	public void makeSound(){
		System.out.println("Animal making sounds");
	}
}

class pig extends Animal{
	public void makeSound(){
		System.out.println("pig says ohi ohi");
	}
}

class dog extends Animal{
	public void makeSound(){
		System.out.println("dog says whang whang");
	}
}

public class InheritTest{
	public static void main(String[] args){
		pig shane = new pig();
		dog logan = new dog();
		shane.makeSound();
		logan.makeSound();
	}
}