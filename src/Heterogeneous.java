//This class helps store heterogeneous data
//(the type of the data could be any one of int, float, etc.) 
public class Heterogeneous {
	public String type;
	public Object value;
	public Heterogeneous(String type){//, Object value){

		this.type = type;
		
		if (this.type.equals("int")){
			this.value = (int) 0;
		} else if (this.type.equals("float")){
			this.value = (float) 0;
		}else if (this.type.equals("bool")){
			this.value = (boolean) false;
		}else{
			this.value = (String) "";
				}

			}


		}
