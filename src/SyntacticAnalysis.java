import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntacticAnalysis {
	//Stores the classified Tokens
	public ArrayList<String> tokens = new ArrayList<String>();
	//Pointer to next token to look at
	private int currentTokenindex = 0;
	//Error Flag
	private boolean error = false;


	//This method is called to start the syntactical analysis of the loaded tokens
	public void verifySyntax(){
		Program();
		if(currentTokenindex < tokens.size()) {error();}
		sucess();
	}


	private void Program(){
		//Makes sure the program has a closing brace
		if(!tokens.get(tokens.size()-1).equals("}")){
			error();
		}
		String required[];
		boolean endsymbol = false;
		required = new String[] {"type","main","(",")","{"};
		for(int i = 0;i < required.length ;i++){
			if(tokens.get(currentTokenindex).equals(required[i])){
				currentTokenindex++;//consumes a token
				if(i == 4 ){ Declarations();}
				while( i == 4 && currentTokenindex < tokens.size() && !endsymbol){ 
					endsymbol = Statements();}
			} else{ error();}
		}
	}


	private void Declarations(){ 
		if(!error){
			//Calls Declaration(); the appropriate number of times. Once for each Declaration.
			int numberOfDeclarations = 0;
			for(int x = 0;  x < tokens.size() ; x++){
				if(tokens.get(x).equals("type")){
					numberOfDeclarations++;
				}
			}
			numberOfDeclarations--;
			for(int i = 0;  i < numberOfDeclarations; i++){
				Declaration();
			}

		}
	}


	private void Declaration(){
		if(tokens.get(currentTokenindex).equals("type")){
			currentTokenindex++;//consumes a token
			if(tokens.get(currentTokenindex).equals("id")){
				currentTokenindex++;//consumes a token
				semiColon();
			}else{
				error();
			}
		} else{
			error();
		}
	}

	private boolean Statements(){
		boolean endsymbol = false;
		endsymbol = Statement();
		while(currentTokenindex < tokens.size() && !endsymbol && !error){
			endsymbol = Statement();
		}
		return endsymbol;

	}

	private boolean Statement(){
		boolean startFromTop = false;
		if(!error){
			if( currentTokenindex >= tokens.size()){
				errorMissingSyntax();
			}
			else{
				//Tells program to terminate
				if(tokens.get(currentTokenindex).equals("}")){
					currentTokenindex++;
				return true;
				}
				
				/*If one of the statements is satisfied by the current string of tokens
				 *it skips over the rest of the other statements and starts from the top again.
				 *This prevents the assignment statement from running on a statement
				 *that is valid, but not an assignment statement (and throwing an error for not matching an assignment).*/ 
				startFromTop = PrintStmt();
				if(!startFromTop && !error){
					startFromTop = IfStatement();
				}
				if(!startFromTop && !error){
					startFromTop = WhileStmt();
				}
				if(!startFromTop && !error){
					startFromTop = ReturnStmt();
				}
				if(!startFromTop && !error){
					Assignment();
				}
			}
		}
		return false;
	}

	private boolean  PrintStmt(){
		if( currentTokenindex < tokens.size()){
			if (tokens.get(currentTokenindex).equals("print")){
				currentTokenindex++;
				Expression();
				semiColon();
				return true;
			}
		}
		return false;
	}

	private boolean IfStatement(){
		if( currentTokenindex < tokens.size()){
			String required[];
			required = new String[] {"if","(",")",};
			for(int i = 0;i < required.length ;i++){
				if(tokens.get(currentTokenindex).equals(required[i])){
					currentTokenindex++;//consumes a token
					if(i ==  1 ){
						Expression();
					}if(i == 2 ){
						Statement();
						if(currentTokenindex < tokens.size() ){
							//else clause
							if (tokens.get(currentTokenindex).equals("else")){
								currentTokenindex++;
								int current = currentTokenindex;
								Statement();
								//makes sure the else is followed by a statement
								if(current == currentTokenindex){
									error();
								}
							}
						}
					}
					return true;
				}
			} 
		}
		return false;
	}


	private boolean WhileStmt(){
		if( currentTokenindex < tokens.size()){
			if (tokens.get(currentTokenindex).equals("while")){
				currentTokenindex++;
				if (tokens.get(currentTokenindex).equals("(")){
					currentTokenindex++;
					Expression();
					if (tokens.get(currentTokenindex).equals(")")){
						currentTokenindex++;
						int current = currentTokenindex;
						Statement();
						//checks to make sure there was a valid statement
						if(current == currentTokenindex){ error();}
					}else{error();}
				}else{error();}
				return true;
			}
		}
		return false;
	}

	private boolean ReturnStmt(){
		if( currentTokenindex < tokens.size()){
			if (tokens.get(currentTokenindex).equals("return")){
				currentTokenindex++;
				Expression();
				semiColon();
				return true;
			}
		}
		return false;
	}


	private void Assignment(){
		if(currentTokenindex < tokens.size()){
			id();
			assignOp();
			Expression();
			semiColon();
		}
	}

	private void semiColon(){
		if(tokens.get(currentTokenindex).equals(";")){
			currentTokenindex++;//consumes a token
			return;
		}else{
			error();
		}
	}

	private void id(){
		if(tokens.get(currentTokenindex).equals("id")){
			currentTokenindex++;//consumes a token
			return;
		} else{
			error();
		}
	}

	private void assignOp(){
		if(tokens.get(currentTokenindex).equals("assignOp")){
			currentTokenindex++;//consumes a token
			return;
		} else{
			error();
		}
	}


	private void Expression(){
		int current = currentTokenindex;
		Conjunction();
		while(tokens.get(currentTokenindex).equals("||") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Conjunction();
		}
		//Makes sure that when this is called that it raises an error if there is no expression 
		if(current == currentTokenindex){
			error();
		}

	}
	private void Conjunction(){
		Equality();
		while(tokens.get(currentTokenindex).equals("&&") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Equality();
		}

	}

	private void Equality(){
		Relation();
		while(tokens.get(currentTokenindex).equals("equOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Relation();
		}
	}


	private void Relation(){
		Addition();
		while(tokens.get(currentTokenindex).equals("relOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Addition();
		}
	}


	private void Addition(){
		Term();
		while(tokens.get(currentTokenindex).equals("addOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Term();
		}
	}


	private void Term(){
		Factor();
		while(tokens.get(currentTokenindex).equals("multOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Factor();
		}
	}


	private void Factor(){
		if (tokens.get(currentTokenindex).equals("id")
				|| tokens.get(currentTokenindex).equals("intLiteral")
				|| tokens.get(currentTokenindex).equals("boolLiteral")
				|| tokens.get(currentTokenindex).equals("floatLiteral")
				|| tokens.get(currentTokenindex).equals("charLiteral")

				){
			currentTokenindex++;
		} else if(tokens.get(currentTokenindex).equals("(")) {
			currentTokenindex++;
			Expression();
			if(tokens.get(currentTokenindex).equals(")")){
				currentTokenindex++;
			}else{
				error();
			}
		}
	}

	private void print(){
		System.out.println("Last Token Pointed To: " + currentTokenindex);

	}
	//Grabs tokens from lexer
	public void getTokensFromLexer(ArrayList<ArrayList<String>> tokens){
		for (int i = 0; i < tokens.size(); i++){
			//removes uneeded comments
			if(!tokens.get(i).get(0).equals("comment")){
				this.tokens.add(tokens.get(i).get(0));
			}
		}
	}

	//Called when an error is caught and reports where it occured
	private void error(){
		error = true;
		System.out.println("");
		System.out.println("There was a syntax error on the " + (currentTokenindex +1) + "th token!");
		if(!(currentTokenindex+1 >= tokens.size())){
			System.out.println("Token: "+ tokens.get(currentTokenindex));
		}
		print();
	}
	//Is called if there is syntax missing is a special case.
	private void errorMissingSyntax(){
		error = true;
		System.out.println("");
		System.out.println("Incomplete Statement: There is missing Syntax. ");
		print();
	}
	//Only prints success when no errors are detected
	private void sucess(){
		System.out.println("");
		if (currentTokenindex == tokens.size() && !error){
			System.out.println("The syntax is correct!");
			print();
		}
	}

	//Grabs tokens from a file that has already been lexed
	public void getTokensFromFile(String path){
		String rawString = "";
		//Read the lexed file
		try {
			rawString = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Removes the tab symbol from the document before further parsing 
		String tabReplacedString = rawString.replaceAll("\\t", " ");
		//removes comments from the file
		String commentReplacedString = tabReplacedString.replaceAll("comment.*\\n", "");
		//Removes the newline symbol from the document before further parsing 
		String newLineReplacedString = commentReplacedString.replaceAll("\\n *", " ");
		//Divides the document into tokens and lexemes
		String[] tokens = newLineReplacedString.split("\\s+");
		//Starts a 2 to strip out field labels if they are there
		int start = 0;
		if (tokens[0].equals("Tokens")){
			start = 2;
		}
		//Adds only the tokens to an array
		for(int i = start; i < tokens.length; i++){
			//strips out empty strings
			if (!tokens[i].equals("")){
				this.tokens.add(tokens[i]);
				i++;
			}
		}
	}
}
