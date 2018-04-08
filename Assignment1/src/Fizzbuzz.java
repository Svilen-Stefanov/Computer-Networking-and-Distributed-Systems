/**
 * Homework 1
 * FizzBuzz
 * written by Svilen Stefanov
 */
import java.util.Scanner;

public class Fizzbuzz {
	public static void main(String[] args) {
		int x=0;
		try{
			x = Integer.parseInt(args[0]);
		} catch(Exception e) {
			System.out.println("Wrong format!");
		}
		
		for (int i = 1; i <= x; i++) {
			if(i%3==0 && i%5==0)
				System.out.println("FizzBuzz");
			else if(i%3==0)
				System.out.println("Fizz");
			else if(i%5==0)
				System.out.println("Buzz");
			else System.out.println(i);
		}
	}
}
