/* C-Lite Compiler written  by Zackery Leman 2014
*/

import java.util.ArrayList;

public class Program {
	
public static void main(String[] args)
{
	LexicalParser parser  =  new LexicalParser();
	long start = System.nanoTime();
	//Can either take a command line argument or use direct input in source code
	if(args.length == 0){
		//Defaults here if no command line argument is provided
		parser.start("/Users/zackleman/Downloads/input.c");
	} else{
		parser.start(args[0]);
	}
	long runTime = System.nanoTime() - start;
	System.out.print("Running Time: " + runTime/1000000 + " ms");
	//ArrayList<ArrayList<String>> tokens = parser.tokens;
}

}