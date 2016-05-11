package yulei.mag.api;

import java.util.ArrayList;
import java.util.List;


public class ReturnResult {
	
	public List<Id2Id_2Hop_FId_One> Id2Id_2Hop_FId_One = new ArrayList<Id2Id_2Hop_FId_One>();
	
	public ReturnResult(){}
	
	public void addFIdPath(Id2Id_2Hop_FId_One Id2Id_2Hop_FId){
		this.Id2Id_2Hop_FId_One.add(Id2Id_2Hop_FId);
	}
}
class Id2Id_2Hop_FId_One{
	public String Id1;
	public String FId;
	public String Id2;
	
	public Id2Id_2Hop_FId_One(String Id1,String FId,String Id2){
		this.Id1 = Id1;
		this.FId = FId;
		this.Id2 = Id2;
	}
}