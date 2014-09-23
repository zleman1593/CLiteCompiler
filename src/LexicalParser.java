/* C-Lite Compiler written  by Zackery Leman 2014
 */

//TODO: What should I so about the single quotations for char literals?
import java.util.ArrayList;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class LexicalParser {
	//Stores the classified Tokens
	public ArrayList<ArrayList<String>> tokens = new ArrayList<ArrayList<String>>();
	//Stores all the information needed to classify tokens
	private  Map tokenDef;
	//Holds all the single line comments in the input file
	private ArrayList<String> comments = new ArrayList<String>();
	private String rawString;
	/*Sets up dictionary, reads in file to parse, and calls parsing methods*/
	public void start(String path) {
		//Adds Tokens and definitions to a dictionary
		setupDictionary();
		//Read the .c file
		try {
			rawString = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Looks at each lexeme and classifies it into the appropriate Token
		this.tokens = classifyTokens(extractTokens());
		//Print the lexeme classification to the console
		print();
	}


	/*
	 * This method identifies all the individual lexemes and returns the lexemes
	 */
	private String[] extractTokens() {
		//Removes the tab symbol from the document before further parsing 
		String tabReplacedString = rawString.replaceAll("\\t", " ");
		//Finds all comments and adds them to an array
		Pattern pat = Pattern.compile("\\//.*\\n");
		Matcher mat = pat.matcher(tabReplacedString);
		while (mat.find()){
			comments.add(mat.group());
		}
		//Removes the comments from the documents and inserts a placeholder token in their place 
		String commentReplacedString = tabReplacedString.replaceAll("\\//.*\\n", " #comment\n ");
		//Removes the newline symbol from the document before further parsing 
		String goodSpacing2 = commentReplacedString.replaceAll("\\n *", " ");
		//These lines take care of multiple spaces
		String goodSpacing1 = addSpaces(goodSpacing2).replaceAll("    ", " ");
		String goodSpacing = goodSpacing1.replaceAll("   ", " ");
		String allLines = goodSpacing.replaceAll("  ", " ");
		//Divides the document into individual lexemes based on spaces between lexemes
		String[] tokens = allLines.split(" ");
		return tokens;
	}

	/*Looks at each lexeme and classifies it into the appropriate Token*/
	private  ArrayList<ArrayList<String>> classifyTokens(String[] tokens){
		//Stores all the lexemes and their associated Token classification
		ArrayList<ArrayList<String>> finishedTokens = new ArrayList<ArrayList<String>>();
		//For all lexemes
		for(int i = 0; i < tokens.length; i++){
			ArrayList<String> tempAddTokenAndClassifier = new ArrayList<String>();
			//Takes care of case when there is no space between last lexeme in the line and ending semicolon
				/*If the token type doesn't match one in the dictionary parse it to identify
				what Token it is by which pattern it matches given the remaining possibilities*/
				String classifier = (String) tokenDef.get(tokens[i]);
				
				if (classifier == null){
					classifier = regex(tokens[i]);
				} else if (classifier.equals("comment")){
					//If lexme is a comment add the comment back, if not, add the lexeme
					tokens[i] = comments.remove(0);
				} 
				if (classifier.equals("charLiteral")){
					//Strips away ''
					tokens[i] = tokens[i].substring(1, 2);
				}
				tempAddTokenAndClassifier.add(classifier);
				tempAddTokenAndClassifier.add(tokens[i]);
				finishedTokens.add(tempAddTokenAndClassifier);

		}
		return finishedTokens;

	}

	/*
	 * This classifies the lexems that cannot be classified
	 *  by matching a predefined string, but rather by matching a pattern.
	 */
	private String regex(String string){
		Pattern p = Pattern.compile("[a-zA-Z](\\d|[a-zA-Z])*");
		Matcher m = p.matcher(string);
		if (m.matches()){
			return "id";
		}
		p = Pattern.compile("\\d+");
		m = p.matcher(string);
		if (m.matches()){
			return "intLiteral";
		}
		p = Pattern.compile("\\d+.\\d*");
		m = p.matcher(string);
		if (m.matches()){
			return "floatLiteral";
		}
		p = Pattern.compile("'(\\d|[a-zA-Z])'");
		m = p.matcher(string);
		if (m.matches()){
			return "charLiteral";
		}
		return "";
	}

	/*Utility method to print the results of lexeme Token classification to the console*/
	private void print()
	{
		System.out.printf("Tokens          Lexemes\n");
		System.out.printf("\n");
		String spaces = " ";
		for (int i = 0; i < tokens.size(); ++i)
		{	
			for( int j = 0; j <= ( 10 - tokens.get(i).get(0).length()); j++){
				spaces += ' ';	
			}
			
			System.out.printf(" %s %s  %s\n",
					tokens.get(i).get(0), spaces, tokens.get(i).get(1)); 
			 spaces = " ";
		}
	}


	/*
	 *Sets up the dictionary that holds most of the lexeme 
	 * Token classification information.
	 */
	private void setupDictionary(){
		// key value pairs
		tokenDef = new HashMap();
		tokenDef.put("main", "main");
		tokenDef.put("bool","type");
		tokenDef.put("char","type");
		tokenDef.put("float","type");
		tokenDef.put("int","type");
		tokenDef.put(";",";");
		tokenDef.put("true", "boolLiteral");
		tokenDef.put("false","boolLiteral");
		tokenDef.put("==","equOp");
		tokenDef.put("!=","equOp");
		tokenDef.put("<","relOp");
		tokenDef.put("<=","relOp");
		tokenDef.put(">","relOp");
		tokenDef.put(">=","relOp");
		tokenDef.put("=","assignOp");
		tokenDef.put("if","if");
		tokenDef.put("else","else");
		tokenDef.put("while","while");
		tokenDef.put("+","addOp");
		tokenDef.put("-","addOp");
		tokenDef.put("*","multOp");
		tokenDef.put("/","multOp");
		tokenDef.put("(","(");
		tokenDef.put(")",")");
		tokenDef.put("{","{");
		tokenDef.put("}","}");
		tokenDef.put("[","[");
		tokenDef.put("]","]");
		tokenDef.put("return","return");
		tokenDef.put("print","print");
		tokenDef.put("#comment","comment");
	}
	
	/*This finds all lexemes that may be adjoined to a neighboring lexeme
	 * and adds a space in between them so that the rest of the parsing can work*/
	private String addSpaces(String goodSpacing){
		ArrayList<String> spaceBuffer = new ArrayList<String>();
		spaceBuffer.add("}");
		spaceBuffer.add("]");
		spaceBuffer.add("<=");
		spaceBuffer.add("!=");
		spaceBuffer.add("-");
		spaceBuffer.add("==");
		spaceBuffer.add(">=");
		spaceBuffer.add("/");
		//Need to escape these
		spaceBuffer.add("\\+");
		spaceBuffer.add("\\*");
		spaceBuffer.add("\\[");
		spaceBuffer.add("\\(");
		spaceBuffer.add("\\)");
		spaceBuffer.add("\\{");
		spaceBuffer.add(";");
		
		goodSpacing = goodSpacing.replaceAll("(((?<!=)(?<!<)(?<!>))=(?!=))"," "+"="+ " ");
		goodSpacing = goodSpacing.replaceAll("<(?!=)"," "+"<"+ " ");
		goodSpacing = goodSpacing.replaceAll(">(?!=)"," "+">"+ " ");
		
		for (int i = 0; i < spaceBuffer.size(); i++){
			goodSpacing = goodSpacing.replaceAll(spaceBuffer.get(i),  " "+spaceBuffer.get(i)+ " ");
		}
		return goodSpacing;
	}
}


//There is no benefit to including these
		/*spaceBuffer.add("//comment");
		 spaceBuffer.add("true");
		spaceBuffer.add("false");
		spaceBuffer.add("if");
		spaceBuffer.add("else");
		spaceBuffer.add("while");
		spaceBuffer.add("return");
		spaceBuffer.add("print");
		spaceBufferBefore.add("int");
		spaceBuffer.add("main");
		spaceBuffer.add("bool");
		spaceBuffer.add("char");	  
		spaceBuffer.add("float");*/
		
