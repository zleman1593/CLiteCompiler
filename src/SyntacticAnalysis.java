import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

public class SyntacticAnalysis {
	//Stores the classified Tokens
	public ArrayList<String> tokens = new ArrayList<String>();
	//Stores the  Lexemes
	public ArrayList<String> lexemes = new ArrayList<String>();
	//Pointer to next token to look at
	private int currentTokenindex = 0;
	//Error Flag
	private boolean error = false;

	//Create HashMap
	private HashMap<String, Heterogeneous> symTable = new HashMap<String, Heterogeneous>();
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
				if (!error){
					//checks to see if duplicate declaration
					if(symTable.get(this.lexemes.get(currentTokenindex-2) ) != null){
						semanticError("duplicate");
					}

				}

				if (lexemes.get(currentTokenindex-3).equals("int")) {
					Heterogeneous v = new Heterogeneous("int");
					symTable.put(this.lexemes.get(currentTokenindex-2), v);
				} else if (lexemes.get(currentTokenindex-3).equals("float")){
					Heterogeneous v = new Heterogeneous("float");
					symTable.put(this.lexemes.get(currentTokenindex-2), v);
				} else if (lexemes.get(currentTokenindex-3).equals("char")){
					Heterogeneous v = new Heterogeneous("char");
					symTable.put(this.lexemes.get(currentTokenindex-2), v);
				}else {
					Heterogeneous v = new Heterogeneous("bool");
					symTable.put(this.lexemes.get(currentTokenindex-2), v);
				}
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
				//Tells program to terminate because it reached the end symbol
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
				Tuple result = Expression();
				semiColon();
				System.out.print(result.value);
				return true;
			}
		}
		return false;
	}

	private boolean IfStatement(){
		Object result;
		if( currentTokenindex < tokens.size()){
			String required[];
			required = new String[] {"if","(",")",};
			for(int i = 0;i < required.length ;i++){
				if(tokens.get(currentTokenindex).equals(required[i])){
					currentTokenindex++;//consumes a token
					if(i ==  1 ){
						result = Expression();
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
		int convertFrom = 0;
		int convertTo = 0;
		if(currentTokenindex < tokens.size()){

			convertTo = id().maxType;
			String varName  =  lexemes.get(currentTokenindex-1);
			assignOp();
			Tuple result =  Expression();
			convertFrom = result.maxType;
			if(convertTo == 4 &&  convertFrom != 4){
				semanticError("Narrowing Conversion is not allowed");
			} else if (convertTo == 5 &&  convertFrom !=  5){
				semanticError("Narrowing Conversion is not allowed");
			}
			else if (convertFrom > convertTo){
				semanticError("Narrowing Conversion is not allowed");
			}
			semiColon();
			if(!error){
				switch (convertTo) {
				case 1: 
					this.symTable.get(varName).value = (int) result.value;
					break;

				case 2:  this.symTable.get(varName).value = Float.parseFloat(result.value.toString());
				break;
				case 4:  this.symTable.get(varName).value = (boolean) result.value;
				//this.symTable.get(varName).value = Boolean.parseBoolean(result.value.toString());
				break;
				case 5:  this.symTable.get(varName).value = (String) result.value;
				break;
				}

			}
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

	private Tuple id(){
		if(tokens.get(currentTokenindex).equals("id")){
			if(symTable.get(this.lexemes.get(currentTokenindex)) == null){
				semanticError("Undefined Variable");
			}
			String type = symTable.get(this.lexemes.get(currentTokenindex)).type;
			currentTokenindex++;//consumes a token

			if (type.equals("int")){
				return new Tuple(1,  symTable.get(this.lexemes.get(currentTokenindex-1)).value );
			} else if (type.equals("float")){
				return new Tuple(2,  symTable.get(this.lexemes.get(currentTokenindex-1)).value );
			}else if (type.equals("bool")){
				return new Tuple(4,  symTable.get(this.lexemes.get(currentTokenindex-1)).value );
			} else if (type.equals("char")){
				return new Tuple(5, symTable.get(this.lexemes.get(currentTokenindex-1)).value);
			}
		} else{
			error();
		}
		return new Tuple(0, (int) 0 );
	}

	private void assignOp(){
		if(tokens.get(currentTokenindex).equals("assignOp")){
			currentTokenindex++;//consumes a token
			return;
		} else{
			error();
		}
	}


	private Tuple Expression(){
		int current = currentTokenindex;

		boolean returnValue = false;
		boolean quickReturn = true;

		int largestType = 0;
		Tuple result = Conjunction();
		largestType = result.maxType;
		if(tokens.get(currentTokenindex).equals("||")){
			quickReturn = false;
			if(result.maxType == 4){
				if (Boolean.parseBoolean(result.value.toString())){
					returnValue = true;
				}
				}else{semanticError("Expression can not be evaluated to a boolean");}
			}
		while(tokens.get(currentTokenindex).equals("||") && currentTokenindex < tokens.size() ){
			
			currentTokenindex++;//consumes a token
			largestType = 4;
			 result = Conjunction();
		
			if(result.maxType == 4){
				if (Boolean.parseBoolean(result.value.toString())){
					returnValue = true;
				}
		
				
			}else{
					semanticError("Expression can not be evaluated to a boolean");
				}

		}
		//Makes sure that when this is called that it raises an error if there is no expression 
		if(current == currentTokenindex){
			error();
		}
		if(!quickReturn){
			return new Tuple(largestType, returnValue);
			} 
			
			return new Tuple(largestType, result.value );
	}
	private Tuple Conjunction(){

		boolean returnValue = true;
boolean quickReturn = true;
		int largestType = 0;
		Tuple result = Equality();
		largestType = result.maxType;
if(tokens.get(currentTokenindex).equals("&&")){
	quickReturn = false;
		if(result.maxType == 4){
			if (!Boolean.parseBoolean(result.value.toString())){
				returnValue = false;
			}}else{semanticError("Expression can not be evaluated to a boolean");}
		}
		while(tokens.get(currentTokenindex).equals("&&") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			largestType = 4;
			 result = Equality();
		
			if(result.maxType == 4){
				if (!Boolean.parseBoolean(result.value.toString())){
					returnValue = false;

				}
				
			}else{
					semanticError("Expression can not be evaluated to a boolean");
				}

		}
		
		if(!quickReturn){
		return new Tuple(largestType, returnValue);
		} 
		
		return new Tuple(largestType, result.value );
	}

	//Todo make this handle more than two comparisions
	private Tuple Equality(){

		boolean truth = false;

		int largestType = 0;
		Tuple resultTuple = Relation();
		int result = resultTuple.maxType;
		largestType = result;
		int firstType = result;
		int secondType = 0;
		boolean returnTruthValue = false;


		String total = resultTuple.value.toString();

		while(tokens.get(currentTokenindex).equals("equOp") && currentTokenindex < tokens.size() ){
			total = resultTuple.value.toString();
			currentTokenindex++;//consumes a token
			largestType = 4;//because it has to be a boolean
			resultTuple = Relation();

			secondType= resultTuple.maxType;
			boolean evaluation  = false;
			returnTruthValue = true;
			//test for two type compatiability

			if ((firstType == 4) && (secondType == 4)){
				evaluation =  Boolean.parseBoolean(total) == Boolean.parseBoolean(resultTuple.value.toString());
			} else if ((firstType == 5) && (secondType == 5)){
				evaluation = total == resultTuple.value.toString();
			} else if  ((firstType <= 2) && (secondType <= 2)){
				evaluation =  (float) Float.parseFloat(total) == (float) Float.parseFloat(resultTuple.value.toString());
			} else{
				semanticError("Incompatible type comparison");
			}

			if (evaluation){
				if(lexemes.get(currentTokenindex - 2).equals("==")){
					truth = true;
				} else{
					truth = false;
				}
			} else{
				if(lexemes.get(currentTokenindex - 2).equals("!=")){
					truth = true;
				} else{
					truth = false;
				}

			}



		}
		//Cases convert string to appropriate value based on largestType
		switch (largestType) {
		case 1: return new Tuple(largestType, Integer.parseInt(total));

		case 2:  return new Tuple(largestType, Float.parseFloat(total));

		case 4:  
			if(returnTruthValue){
				return new Tuple(largestType, truth);
			}else{
				return new Tuple(largestType, Boolean.parseBoolean(total));
			}


		case 5:  return new Tuple(largestType, total);

		}
		return new Tuple(largestType, total);

	}


	private Tuple Relation(){

		boolean evaluation = false;
		int largestType = 0;
		Tuple resultTuple = Addition();
		int result = resultTuple.maxType;

		if (result>largestType){
			largestType = result;
		}
		//Char and Bool can not be compared
		if (largestType == 4 || largestType == 5){
			if (tokens.get(currentTokenindex).equals("relOp")){
				semanticError("Incompatible Operand for relOp");
			}
			if(largestType == 4){
				return new Tuple(largestType,(boolean) resultTuple.value);
			} else if(largestType == 5){
				return new Tuple(largestType, (String) resultTuple.value);
			}
		}
		//If int or float and there is no relative operator
		if ( largestType == 1 && !tokens.get(currentTokenindex).equals("relOp")){
			return new Tuple(largestType,(int) resultTuple.value);
		} else if (largestType == 2 && !tokens.get(currentTokenindex).equals("relOp")){

			return new Tuple(largestType,(float) Float.parseFloat(resultTuple.value.toString()));
		}

		float total = (float) Float.parseFloat(resultTuple.value.toString());
		while(tokens.get(currentTokenindex).equals("relOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token

			resultTuple = Addition();
			float numericalResult = Float.parseFloat(resultTuple.value.toString());
			if(lexemes.get(currentTokenindex - 2).equals("<")){
				evaluation =  total < numericalResult;
			} else if (lexemes.get(currentTokenindex - 2).equals(">")){
				evaluation =  total > numericalResult;
			} else if (lexemes.get(currentTokenindex - 2 ).equals("<=")){
				evaluation =  total <= numericalResult;
			}else if (lexemes.get(currentTokenindex - 2).equals(">=")){
				evaluation =  total >= numericalResult;
			}
			//pass the value along for the next comparison
			total = numericalResult;
		}

		if(evaluation){
			return new Tuple(4, (boolean) true);
		} else{
			return new Tuple(4, (boolean) false);
		}
	}

	/*

	private Tuple Relation(){

		float total = 0;
		boolean evaluation = false;
		int largestType = 0;
		Tuple resultTuple = Term();
		int result = resultTuple.maxType;
		if (result>largestType){
			largestType = result;
		}
		while(tokens.get(currentTokenindex).equals("relOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token

			result = Addition().maxType;
			if (result>largestType){
				largestType = result;
			}
		}
		if(evaluation){
				return new Tuple(4, (boolean) true);
			} else{
				return new Tuple(4, (boolean) false);
			}
	}
	 */
	private Tuple Addition(){
		int largestType = 0;
		Tuple resultTuple = Term();
		int result = resultTuple.maxType;
		if (result>largestType){
			largestType = result;
		}
		/*Char and Bool and not be added or subtracted*/
		if (largestType == 4 || largestType == 5){
			if (tokens.get(currentTokenindex).equals("addOp")){
				semanticError("Incompatible Operand for addOp");
			}
			if(largestType == 4){
				return new Tuple(largestType,(boolean) resultTuple.value);
			}//Then it equals 5
			return new Tuple(largestType, (String) resultTuple.value);
		}
		float total = (float) Float.parseFloat(resultTuple.value.toString());
		while(tokens.get(currentTokenindex).equals("addOp") && currentTokenindex < tokens.size() ){
			int sign = 1;
			if (lexemes.get(currentTokenindex).equals("-")){
				sign = -1;
			}
			currentTokenindex++;//consumes a token
			resultTuple = Term();
			result = resultTuple.maxType;

			float numericalResult = (float) Float.parseFloat(resultTuple.value.toString());
			if (result>largestType){
				largestType = result;
			}
			total = (float) (total + (sign*numericalResult));
		} 
		if(largestType == 1){
			return new Tuple(largestType, (int) total);
		} else{
			return new Tuple(largestType, total);
		}
	}


	private Tuple Term(){

		int largestType = 0;
		Tuple resultTuple = Factor();
		int result = resultTuple.maxType;

		if (result>largestType){
			largestType = result;
		}
		/*Char and Bool and not be multiplied*/
		if (largestType == 4 || largestType == 5){
			if (tokens.get(currentTokenindex).equals("multOp")){
				semanticError("Incompatible Operand for multOp");
			}
			if(largestType == 4){
				return new Tuple(largestType,(boolean) resultTuple.value);
			}//Then it equals 5
			return new Tuple(largestType, (String) resultTuple.value);
		}
		float total = (float) Float.parseFloat(resultTuple.value.toString());

		while(tokens.get(currentTokenindex).equals("multOp") && currentTokenindex < tokens.size() ){
			int exponent = 1;
			if (lexemes.get(currentTokenindex).equals("/")){
				exponent = -1;
			}
			currentTokenindex++;//consumes a token

			resultTuple = Factor();
			result = resultTuple.maxType;


			float numericalResult = (float) Float.parseFloat(resultTuple.value.toString());
			if (result>largestType){
				largestType = result;
			}
			total = (float) (total * Math.pow(numericalResult , exponent));
		} 
		if(largestType == 1){
			return new Tuple(largestType, (int) total);
		} else{
			return new Tuple(largestType, total);
		}
	}


	private Tuple Factor(){
		if(tokens.get(currentTokenindex).equals("id")){
			if(symTable.get(this.lexemes.get(currentTokenindex)) == null){
				semanticError("Undefined Variable");
			}

			String type = symTable.get(this.lexemes.get(currentTokenindex)).type;
			currentTokenindex++;
			if (type.equals("int")){
				return new Tuple(1,  symTable.get(this.lexemes.get(currentTokenindex-1)).value );
			} else if (type.equals("float")){
				return new Tuple(2,  symTable.get(this.lexemes.get(currentTokenindex-1)).value );
			}else if (type.equals("bool")){
				return new Tuple(4,  symTable.get(this.lexemes.get(currentTokenindex-1)).value );
			} else if (type.equals("char")){
				return new Tuple(5, symTable.get(this.lexemes.get(currentTokenindex-1)).value);
			}

		} else if ( tokens.get(currentTokenindex).equals("intLiteral")){
			currentTokenindex++;
			return new Tuple(1, (int)  Integer.parseInt(this.lexemes.get(currentTokenindex-1)));
		} else if (tokens.get(currentTokenindex).equals("boolLiteral")){
			currentTokenindex++;
			return new Tuple(4, (boolean) Boolean.parseBoolean(this.lexemes.get(currentTokenindex-1)));
		} else if (tokens.get(currentTokenindex).equals("floatLiteral")){
			currentTokenindex++;
			return new Tuple(2, (float)  Float.parseFloat(this.lexemes.get(currentTokenindex-1)));

		} else if ( tokens.get(currentTokenindex).equals("charLiteral")){
			currentTokenindex++;
			return new Tuple(5, (String) this.lexemes.get(currentTokenindex-1));
		} else if(tokens.get(currentTokenindex).equals("(")) {
			currentTokenindex++;
			Tuple result = Expression();
			if(tokens.get(currentTokenindex).equals(")")){
				currentTokenindex++;
				return result;
			}else{
				error();
			}
		}
		return new Tuple(0, (int) 0 );
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	private void print(){
		System.out.println("Last Token Pointed To: " + currentTokenindex);

	}
	//Grabs tokens from lexer
	public void getTokensFromLexer(ArrayList<ArrayList<String>> tokens){
		for (int i = 0; i < tokens.size(); i++){
			//removes uneeded comments
			if(!tokens.get(i).get(0).equals("comment")){
				this.tokens.add(tokens.get(i).get(0));
				this.lexemes.add(tokens.get(i).get(1));
			}
		}
	}

	//Called when an error is caught and reports where it occurred
	private void error(){
		if (!error){
			error = true;
			System.out.println("");
			System.out.println("There was a syntax error on the " + (currentTokenindex +1) + "th token!");
			if(!(currentTokenindex+1 >= tokens.size())){
				System.out.println("Token: "+ tokens.get(currentTokenindex));
			}
			print();
		}
	}
	//Called when a semantic error is caught 
	private void semanticError(String type){
		error = true;
		System.out.println("");
		System.out.println("There was a semantic error on the " + (currentTokenindex +1) + "th token!");
		System.out.println("Type Error: " + type);
		if(!(currentTokenindex+1 >= tokens.size())){
			System.out.println("Lexeme: "+ lexemes.get(currentTokenindex));
		}
		print();
		System.exit(0);
	}
	//Is called if there is syntax missing is a special case.
	private void errorMissingSyntax(){
		error = true;
		System.out.println("");
		System.out.println("Incomplete Statement: There is missing Syntax. ");
		print();
		System.exit(0);
	}
	//Only prints success when no errors are detected
	private void sucess(){
		System.out.println("");
		if (currentTokenindex == tokens.size() && !error){
			System.out.println("The syntax is correct!");
			print();
		}
		System.exit(0);
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
				this.lexemes.add(tokens[i]);
			}
		}
	}
}
