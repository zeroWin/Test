package yulei.mag.api;

import yulei.mag.api.ResultJsonClass.Entities;

public class SolutionAuIdToAuId {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	public SolutionAuIdToAuId(){
		apiuse = new APIuse();
	};
	
	
	public String AuIdToAuId_All(String id1,String id2)
	{
		String result = "";

		apiuse.setCount("50000");
		apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		apiuse.setOffset("0");
		// 1-hop
	
		
		
		String expr = "Id="+id1;
		apiuse.setExpr(expr);
		
//		apiuse.setCount("10000");
//		apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		
		// Get id1 search result
        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
        Entities EntitiesId1 = searchResultId.entities.get(0);
        
        // Get id2 search result
		expr = "Id="+id2;
		apiuse.setExpr(expr);
		searchResultId = apiuse.HandleURI(apiuse.getURI());
		//apiuse.showResultAsJsonFormat(searchResultId);
		Entities EntitiesId2 = searchResultId.entities.get(0);
		
		
		// 2-hop
		String path2Hop = "";//IdToId_2Hop(id1, id2,EntitiesId1,EntitiesId2);
		if(path2Hop.length() != 0)
			result += path2Hop + ",";
		
		// 3-hop
		String path3Hop = "";//IdToId_3Hop(id1,id2,EntitiesId1,EntitiesId2);
		if(path3Hop.length() != 0)
			result += path3Hop + ",";
		
		int t = result.length();
		if(t != 0) // 去掉最后的逗号
			return result.substring(0, result.length()-1);
		return result;
	}
}
