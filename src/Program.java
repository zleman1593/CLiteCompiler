/* C-Lite Compiler written  by Zackery Leman 2014
 */

import java.io.IOException;
import java.util.ArrayList;

public class Program {

	public static void main(String[] args) throws IOException
	{
		
		LexicalParser parser  =  new LexicalParser();
		long start = System.nanoTime();
		//Can either take a command line argument or use direct input in source code
		if(args.length == 0){
			//Defaults here if no command line argument is provided
			parser.start("/Users/zackleman/Documents/workspace/CLiteCompiler1/src/input.c");
		} else{
			parser.start(args[0]);
		}
		long runTime = System.nanoTime() - start;
		System.out.println("Running Time of Lexer: " + runTime/1000000 + " ms");
		parser.print();
		parser.printLine();
		ArrayList<ArrayList<String>> tokens = parser.tokens;
		
		SyntacticAnalysis syntax = new SyntacticAnalysis();
		
		syntax.getTokensFromLexer(tokens);
		//OR
		// The program can also write to a file and then read it in. If this is what you want then comment out the above line and uncomment the below line
		//syntax.getTokensFromFile(parser.outPutPath +"/LexicalOutput.txt");
		
	//TO LOAD LEXED FILE
		//syntax.getTokensFromFile(args[0]);
		
		
		syntax.verifySyntax();
	}

}