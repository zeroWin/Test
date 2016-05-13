package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;

public class SolutionAuIdToId {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	public SolutionAuIdToId(){
		apiuse = new APIuse();
	};
	
	
	public String AuIdToId_All(String AuId1,String Id2)
	{
		String result = null;
		
		// 差条件
		apiuse.setCount("50000"); // 个数
		apiuse.setAttributes("Id,C.CId,F.FId,J.JId,AA.AuId,AA.AfId,RId"); // 返回的东西，根据路径分析图，只需要这四样数据
		apiuse.setOffset("0"); // 偏移
		

		
		// 得到AuId1需要的数据
		String expr = "Composite(AA.AuId="+AuId1+")";
		apiuse.setExpr(expr);
        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
        List<Entities> EntitiesAuId1 = searchResultId.entities;
        
        // 得到AuId2需要的数据
		expr = "Id="+Id2+")";
		apiuse.setExpr(expr);
        searchResultId = apiuse.HandleURI(apiuse.getURI());
        Entities entitiesId2 = searchResultId.entities.get(0);    
        
		// 1-hop 1-hop的路径"" 或 [],
		String path1Hop = AuIdToId_1Hop(AuId1,Id2,entitiesId2);//AuIdToAuId_2Hop(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2);

		// 2-hop 返回""或者[],
		String path2Hop = AuIdToId_2Hop(AuId1,Id2,EntitiesAuId1);//AuIdToAuId_2Hop(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2)
		
		// 3-hop 返回""着这[],
		String path3Hop = "";//AuIdToAuId_3Hop(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2);

		
		result = new StringBuilder(path1Hop.length()+path2Hop.length()+path3Hop.length())
				.append(path1Hop).append(path2Hop).append(path3Hop).toString();
		
		int t = result.length();
		if(t != 0) // 去掉最后的逗号 
			return result.substring(0, t-1);
		return result;
	}
	
	/**
	 * 用来判断1-hop，AuId1 -> id2的一跳路径 只有1种情况
	 * 1.id2这篇论文的作者是AuId1
	 * @param Auid1
	 * @param id2
	 * @return [AuId1,Id2] 或者 ""
	 */
	public String AuIdToId_1Hop(String AuId1,String id2,Entities entitiesId2)
	{
		long st = System.nanoTime();
		System.out.println("1-Hop start");
		String result = null;
		

		for(Author author : entitiesId2.AA)
		{
			if(author.AuId.equals(AuId1)) // 找到了作者是AuId1
			{
				result = concatString("[",AuId1,",",id2,"],","");
				System.out.println(result);
				break;
			}
		}
		
		System.out.println("1-Hop end and total times ："+(System.nanoTime()-st));	
		if(result == null)
			return "";
		return result;	
	}
	
	
	// 高效率拼接字符串用
    public static String concatString(String s1, String s2, String s3, String s4, String s5, String s6) {
      return new StringBuilder(s1.length() + s2.length() + s3.length() + s4.length() + s5.length() + s6.length())
              .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString();

  }
    
    
	/**
	 * 用来判断2-hop，AuId1 -> id2的2跳路径 只有1种情况
	 * 1.AuId->Id->RId 即作者是AuId1且引用了id2的所有论文
	 * @param Auid1
	 * @param id2
	 * @return [AuId1,Id,RId(id2)] 或者 ""
	 */
	public String AuIdToId_2Hop(String AuId1,String id2,List<Entities> EntitiesAuId1)
	{
		long st = System.nanoTime();
		System.out.println("2-Hop start");
		StringBuilder result = new StringBuilder();
		
		// 先计算作者1所有论文共引用了多少篇论文。
		// 不能用hashMap因为要同时记录key=RID,Value=Id1,key可能重复，直接equal
		// 这里的算长度没什么用，就是玩一波
		// 不可能有Id是ld2并且RId也是Id2的情况
		// 但Id是Id2的情况还是存在，所以跳过
		long AuIdPaperRidNum = 0;
		long AuIdParprRidEqualId = 0;

		for(Entities entities : EntitiesAuId1)
		{
			int numRId = entities.RId.size();
			if( numRId == 0) // 这个实体没有参考文献
				continue;
			
			AuIdPaperRidNum += numRId;
			if(entities.Id.equals(id2))	//论文是id2，直接跳过，RId不可能再是Id2
				continue;
			
			for(String everyRId:entities.RId) // 搜索每一个Id
			{
				if(everyRId.equals(id2)) // 搜索到了
				{
					result
					.append("[")
					.append(AuId1).append(",")
					.append(entities.Id).append(",")
					.append(everyRId).append("],");
					AuIdParprRidEqualId++;
					break; // RId不会重复，只要搜索到了，就跳出循环
				}
			}
		}
		System.out.println("AuId1="+AuId1+"所写论文个数为"+EntitiesAuId1.size()+"引用其他论文个数为："+AuIdPaperRidNum);
		System.out.println("其中引用id2的论文篇数为："+ AuIdParprRidEqualId);
		System.out.println("2-Hop end and total times ："+(System.nanoTime()-st));
		
	
		return result.toString();
	}
	
	
	/**
	 * 用来判断3-hop，AuId1 -> id2的3跳路径 2种大的情况
	 * 1.AuId->AfId->AuIdx->Id2 即找Id2的作者和Id2的作者属于同一个领域，AuIdx和AuId相等的情况要排除
	 * 1.AuId->id 找id到id2的所有2跳路径，需要排除id=id2的情况
	 * @param Auid1
	 * @param id2
	 * @return [AuId1,AfId,AuIdx,Id2],[AuId1,idx-->两跳 -->id2] 或者 ""
	 */
	public String AuIdToId_3Hop(String AuId1,String id2,List<Entities> EntitiesAuId1)
	{
		long st = System.nanoTime();
		System.out.println("3-Hop start");
		StringBuilder result = new StringBuilder();
		

		
		
		
		
		System.out.println("3-Hop end and total times ："+(System.nanoTime()-st));
		
	
		return result.toString();
	}	
}
