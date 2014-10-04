Readme:

If using an IDE:————————————————————————————————————————————————————————


In the Program class in the line:
 	parser.start("/Users/zackleman/Downloads/input.c");
 	
 	
 Replace the string  with the location of the .c file to parse.
 Then run the program using the eclipse IDE or another IDE.
 
If using the Command Line:————————————————————————————————————————————————————————

 
   Run from the terminal with a single argument (A string of the file location) such as: 
  
    	java Program /Users/zackleman/Downloads/input.c



(If needed recompile Program.java)


ONE ASSUMPTION for the Lexer: That after a one line comment that a new line is made (so that there is a newline character)



IF you are trying to  load an already lexically processed file for syntactical analysis, then:
 1. Uncomment the line in the program class labeled with: //TO LOAD LEXED FILE
 
 2. Comment out all the code above where the syntacic analyzer class is declared.
 
 3.Run the from the command line after compiling with the single argument being the location of the lexed file 
 
 



