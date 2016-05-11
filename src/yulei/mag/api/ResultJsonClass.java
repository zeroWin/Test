package yulei.mag.api;

import java.util.ArrayList;
import java.util.List;

public class ResultJsonClass {
	
	public String expr;
	public List<Entities> entities = new ArrayList<Entities>();
	
	public static class Entities{
		public String logprob;
		public String Id;
		public List<String> RId = new ArrayList<String>();
		public List<Author> AA  = new ArrayList<Author>();
		public Conference C;
		public List<Field> F  = new ArrayList<Field>();
		public Journal J;
	}

	public static class Author{
		public String AuId;
		public String AfId;
		
	}
	public static class Conference{
		public String CId;
		public Conference(String CId){
			this.CId=CId;
		}
	}

	public static class Journal{
		public String JId;
		public Journal(String JId){
			this.JId=JId;
		}
	}

	public static class Field{
		public String FId;
		public Field(String FId){
			this.FId=FId;
		}
	}
}

