import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntacticAnalysis {
	//Stores the classified Tokens
	public ArrayList<String> tokens = new ArrayList<String>();
	private int currentTokenindex = 0;
	private boolean error = false;


	public void getTokensFromLexer(ArrayList<ArrayList<String>> tokens){

		for (int i = 0; i < tokens.size(); i++){
			this.tokens.add(tokens.get(i).get(0));
			//TODO -----------STRIP OUT COmMENTS
		}
	}

	public void getTokensFromFile(String path){
		String rawString = "";
		//Read the .c file
		try {
			rawString = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Removes the tab symbol from the document before further parsing 
		String tabReplacedString = rawString.replaceAll("\\t", " ");
		//TODO:Finds and removes comments --------------------------------------------------------------------
		//TODO:recomment This--------------------------------------------------------------------
		//String commentReplacedString = tabReplacedString.replaceAll("\\//.*\\n", " #comment\n ");
		//Removes the newline symbol from the document before further parsing 
		String newLineReplacedString = tabReplacedString.replaceAll("\\n *", " ");
		//This line take care of multiple spaces
		//Divides the document into individual lexemes based on spaces between lexemes
		String[] tokens = newLineReplacedString.split("\\s+");
		//starts a 2 to strip out field labels
		for(int i = 2; i < tokens.length; i++){
			//strips out spaces
			if (!tokens[i].equals("")){
				this.tokens.add(tokens[i]);
				i++;
			}
		}

	}
	private void error(){
			error = true;
			System.out.println("");
			System.out.println("There was a syntax error on the " + (currentTokenindex +1) + "th token!");
			System.out.println("Token: "+ tokens.get(currentTokenindex));
			print();
	}
	private void errorMissingSyntax(){
		error = true;
		System.out.println("");
		System.out.println("Incomplete Statement: There is missing Syntax. ");
		print();
	}

	private void sucess(){
		System.out.println("");
		if (currentTokenindex == tokens.size() && !error){
			System.out.println("The syntax is correct!");
			print();
		}
	}

	public void verifySyntax(){
		Program();
		if(currentTokenindex < tokens.size()) {error();}
		sucess();
	}


	public void Program(){
		String required[];
		boolean endsymbol = false;
		required = new String[] {"type","main","(",")","{"};//TODO: Add detection for missing bracket
		for(int i = 0;i < required.length ;i++){
			if(tokens.get(currentTokenindex).equals(required[i])){
				currentTokenindex++;//consumes a token
				if(i == 4 ){ Declarations();}
				while( i == 4 && currentTokenindex < tokens.size() && !endsymbol){ 
					endsymbol = Statements();}
			} else{ error();}
		}


}


public void Declarations(){ 
	if(!error){
		//Calls Declaration(); the appropriate number of times. Once for each Declaration.
		int numberOfDeclarations = 0;
		for(int x = 0;  x < tokens.size() ; x++){
			if(tokens.get(x).equals("type")){
			numberOfDeclarations++;
			}
		}
		numberOfDeclarations--;
		System.out.println(numberOfDeclarations);
for(int i = 0;  i < numberOfDeclarations; i++){
			Declaration();
		}
		
	}
	}

	
	public void Declaration(){
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
		if( currentTokenindex >= tokens.size()){//ERROR CHECK
			errorMissingSyntax();
		}
		else{
			if(tokens.get(currentTokenindex).equals("}")){
				currentTokenindex++;
			return true;
			}
			startFromTop = PrintStmt();
			if(!startFromTop){
			startFromTop = IfStatement();
			}
			if(!startFromTop){
			startFromTop = WhileStmt();
			}
			if(!startFromTop){
			startFromTop = ReturnStmt();
			}
			if(!startFromTop){
			 Assignment();//startFromTop = Assignment();

			}

		}
		}
		return false;
	}

	private boolean  PrintStmt(){
		if( currentTokenindex < tokens.size()){// && !error){
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
		if( currentTokenindex < tokens.size()  && !error){
		String required[];
		required = new String[] {"if","(",")",};
		for(int i = 0;i < required.length ;i++){
			if(tokens.get(currentTokenindex).equals(required[i])){
				currentTokenindex++;//consumes a token
				if(i == 1 ){
					Expression();
				}if(i == 2 ){
					Statement();//TODO: Should this be statements?----------------------------------
					if(currentTokenindex < tokens.size() ){
						//else clause
						if (tokens.get(currentTokenindex).equals("else")){
							currentTokenindex++;
							Statement();
							
						}
					}
				}//else{error();} TODO:this
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
						Statement();//TODO detect if the while is missing a statement
					}else{error();}

				}else{error();}
				return true;
			}

		}
		return false;
	}

	private boolean ReturnStmt(){
		if( currentTokenindex < tokens.size() && !error){
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
		if( currentTokenindex < tokens.size()){
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
			error();//errorSemi();
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
		Conjunction();
		while(tokens.get(currentTokenindex).equals("||") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Conjunction();
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

				return;
			}else{
				error();
			}


		}
		
	}


	public void print(){
		System.out.println("Last Token Pointed To: " + currentTokenindex);

	}
}
