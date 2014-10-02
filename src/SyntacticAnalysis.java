import java.util.ArrayList;


public class SyntacticAnalysis {
	//Stores the classified Tokens
	public ArrayList<String> tokens = new ArrayList<String>();

private int currentTokenindex = 0;


	public void getTokens(ArrayList<ArrayList<String>> tokens){
		
		for (int i = 0; i < tokens.size(); i++){
		this.tokens.add(tokens.get(i).get(0));
		//TODO -----------STRIP OUT COmMENTS
		}
	}

private void error(){
	System.out.println("Error");
	/*if (currentTokenindex < tokens.size()){
		System.out.println("Error");
	}*/
}
	public void verifySyntax(){
		Assignment();
	
	}

	private void Assignment(){
		id();
		assignOp();
		Expression();
		semiColon();
		if(currentTokenindex < tokens.size()) {error();}
	}

	private void semiColon(){
		if(tokens.get(currentTokenindex).equals(";")){
		currentTokenindex++;//consumes a token
		return;
		}
	}
	
	private void id(){
		if(tokens.get(currentTokenindex).equals("id")){
		currentTokenindex++;//consumes a token
		return;
		}
	}

	private void assignOp(){
		if(tokens.get(currentTokenindex).equals("assignOp")){
			currentTokenindex++;//consumes a token
			return;
			}
	}
	

	private void Expression(){
		Conjunction();
		while(tokens.get(currentTokenindex).equals("||") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Conjunction();
		}
		//currentTokenindex++;
		//currentTokenindex = currentTokenindex + 2;
		
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
/*
	private void equOp(){
		currentTokenindex++;
		return;

	}
*/

	private void Relation(){
		Addition();
		while(tokens.get(currentTokenindex).equals("relOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Addition();
		}

	}
/*
	private void relOp(){

	}
	*/

	private void Addition(){
		Term();
		while(tokens.get(currentTokenindex).equals("addOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Term();
		}

	}
/*
	private void addOp(){

	}
	*/
	
	private void Term(){
		Factor();
		while(tokens.get(currentTokenindex).equals("multOp") && currentTokenindex < tokens.size() ){
			currentTokenindex++;//consumes a token
			Factor();
		}
	}
/*
	private void multOp(){

	}
	*/

	private void Factor(){//What is the  Identifier here?-------------------------------------------------------------------------------
		if (tokens.get(currentTokenindex).equals("Identifier")
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
			}
	
			
		}
		else{
			//System.out.println("Should trigger error. I think");
			error();
			
		}
	}


	public void print(){
System.out.println(currentTokenindex);

	}
}
