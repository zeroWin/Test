package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.ResultJsonClass.Field;

public final class SolutionAuIdToId {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	public SolutionAuIdToId(){
		apiuse = new APIuse();
	};
	
	// 总共三次请求完成所有操作
	public String AuIdToId_All(String AuId1,String Id2,List<Entities> EntitiesAuId1,Entities EntitiesId2)
	{
		//long st = System.nanoTime();
		//System.out.println("Find start");
//		String result = null;
		 
        
		// 1-hop 1-hop的路径"" 或 [],
		String path1Hop = AuIdToId_1Hop(AuId1,Id2,EntitiesId2);

		// 2-hop 返回""或者[],
		String path2Hop = AuIdToId_2Hop(AuId1,Id2,EntitiesAuId1);
		
		// 3-hop 返回""着这[],
		String path3Hop = AuIdToId_3Hop(AuId1,Id2,EntitiesAuId1,EntitiesId2);

		
		String result = new StringBuilder(path1Hop.length()+path2Hop.length()+path3Hop.length())
				.append(path1Hop).append(path2Hop).append(path3Hop).toString();
//		result = new StringBuilder(path1Hop.length())
//		.append(path1Hop).toString();
		//System.out.println("Find end and total times ："+(System.nanoTime()-st));	
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
	 * @return [AuId1,Id2], 或者 ""
	 */
	public String AuIdToId_1Hop(String AuId1,String id2,Entities entitiesId2)
	{
		//long st = System.nanoTime();
		//System.out.println("1-Hop start");
		String result = null;
		

		for(Author author : entitiesId2.AA)
		{
			if(author.AuId.equals(AuId1)) // 找到了作者是AuId1
			{
				result = concatString("[",AuId1,",",id2,"],","");
				//System.out.println(result);
				break;
			}
		}
		
		//System.out.println("1-Hop end and total times ："+(System.nanoTime()-st));	
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
	 * @return [AuId1,Id,RId(id2)], 或者 ""
	 */
	public String AuIdToId_2Hop(String AuId1,String id2,List<Entities> EntitiesAuId1)
	{
		//long st = System.nanoTime();
		//System.out.println("2-Hop start");
		StringBuilder result = new StringBuilder();
		
		// 先计算作者1所有论文共引用了多少篇论文。
		// 不能用hashMap因为要同时记录key=RID,Value=Id1,key可能重复，直接equal
		// 这里的算长度没什么用，就是玩一波
		// 不可能有Id是ld2并且RId也是Id2的情况
		// 但Id是Id2的情况还是存在，所以跳过
		//long AuIdPaperRidNum = 0;
		//long AuIdParprRidEqualId = 0;

		for(Entities entities : EntitiesAuId1)
		{
			int numRId = entities.RId.size();
			if( numRId == 0) // 这个实体没有参考文献
				continue;
			
			//AuIdPaperRidNum += numRId;
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
					//AuIdParprRidEqualId++;
					break; // RId不会重复，只要搜索到了，就跳出循环
				}
			}
		}
		//System.out.println("AuId1="+AuId1+"所写论文个数为"+EntitiesAuId1.size()+"引用其他论文个数为："+AuIdPaperRidNum);
		//System.out.println("其中引用id2的论文篇数为："+ AuIdParprRidEqualId);
		//System.out.println("2-Hop end and total times ："+(System.nanoTime()-st));
		
	
		return result.toString();
	}
	
	
	/**
	 * 用来判断3-hop，AuId1 -> id2的3跳路径 2种大的情况
	 * 1.AuId->AfId->AuIdx->Id2 即找Id2的作者和Id2的作者属于同一个领域，AuIdx和AuId相等的情况要排除
	 * 2.AuId->id 找id到id2的所有2跳路径，需要排除id=id2的情况
	 * @param Auid1
	 * @param id2
	 * @return [AuId1,AfId,AuIdx,Id2],[AuId1,idx-->两跳 -->id2] 或者 ""
	 */
	public String AuIdToId_3Hop(String AuId1,String id2,List<Entities> EntitiesAuId1,Entities entitiesId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop start");
		String result = null;
		

		// 1.AuId->AfId->AuIdx->Id2 即找Id2的作者和Id2的作者属于同一个领域，AuIdx和AuId相等的情况要排除
		// 返回[],或者""
		String result1 = AuIdToId_3Hop_Rule1(AuId1,id2,EntitiesAuId1,entitiesId2);
		
		// 2.AuId->id 找id到id2的所有2跳路径，需要排除id=id2的情况 
		// 返回[],或者""
		String result2 = AuIdToId_3Hop_Rule2(AuId1,id2,EntitiesAuId1,entitiesId2);
		
		result = new StringBuilder(result1.length()+result2.length())
				.append(result1).append(result2).toString();
		//System.out.println("3-Hop end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}	
	
	
	/**
	 * 用来判断3-hop，AuId1 -> id2的3跳路径 规则1
	 * 1.AuId->AfId->AuIdx->Id2 即找Id2的作者和Id2的作者属于同一个领域，AuIdx和AuId相等的情况要排除
	 * @param Auid1
	 * @param id2
	 * @return [AuId1,AfId,AuIdx,Id2],或者 ""
	 */
	public String AuIdToId_3Hop_Rule1(String AuId1,String id2,List<Entities> EntitiesAuId1,Entities entitiesId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule1 start");
		StringBuilder result = new StringBuilder();
		
		// 先找AuId所有可能属于的组织，这里的大小就是显示一波，没什么用
		Map<String,Integer> map = new HashMap<String,Integer>((int)(EntitiesAuId1.size()/0.75));
		//System.out.println("AuId1="+AuId1+"所写论文个数为："+EntitiesAuId1.size());
		//System.out.println("Id2="+id2+"作者个数为："+entitiesId2.AA.size());
		for(Entities entities : EntitiesAuId1)	// 将作者1所有的可能在的领域领域放到图中
		{
			for(Author author : entities.AA ) //遍历作者
			{
				if(author.AuId.equals(AuId1)) //该作者是AuId1
				{
					if(author.AfId != null)	//有所属组织
					{
						//System.out.println(author.AfId);
						if(map.get(author.AfId) == null)// 如果map中没有加入到map中
							map.put(author.AfId, 1); // 把AfId加入到map中
					}
					break;	//跳出此次循环
				}
			}

		}
		if(map.isEmpty())	//图是空直接返回
		{
			//System.out.println("AuId1不属于任何一个组织 3-Hop-Rule1 end and total times :"+(System.nanoTime()-st));
			return "";
		}
		
		// 找id2的作者中有图中组织的，没找出id2的作者所有可能在的组织，但先放放，先搞其他的
		for(Author author : entitiesId2.AA) // 遍历每一个作者
		{
//			if(author.AuId.equals(AuId1)) // 排除到论文作者是AuId1的情况
//				continue;
			
			if(map.get(author.AfId) != null) // 这个作者在图中找到了组织，存在路径
				result.append("[")
					.append(AuId1).append(",")
					.append(author.AfId).append(",")
					.append(author.AuId).append(",")
					.append(id2).append("],");
		}
		
		//System.out.println("AuId1="+AuId1+"会归属于"+map.size()+"个组织");		
		//System.out.println("3-Hop rule1 end and total times ："+(System.nanoTime()-st));
		return result.toString();
		
	}
	
	
	/**
	 * 用来判断3-hop，AuId1 -> id2的3跳路径 规则3
	 * 2.AuId->id 找id到id2的所有2跳路径，需要排除id=id2的情况，其实不用
	 * @param Auid1
	 * @param id2
	 * @return [AuId1,AfId,AuIdx,Id2],或者 ""
	 */
	public String AuIdToId_3Hop_Rule2(String AuId1,String id2,List<Entities> EntitiesAuId1,Entities entitiesId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule2 start");
		StringBuilder result = new StringBuilder();
        List<Entities> EntitiesRid2 = null;
		// 首先判断AuId1写的论文有没有引用文献
		long AuId1RIdNum = 0;
		for(Entities entitiesAuId1 : EntitiesAuId1)
			AuId1RIdNum += entitiesAuId1.RId.size();
		
		if(AuId1RIdNum != 0) // 有参考文献才搜索
		{
			// 搜索所有引用了id2的文献Id
			apiuse.setAttributes("Id"); // 返回的东西，根据路径分析图，只需要Id
			String expr = "RId="+id2;
			apiuse.setExpr(expr);
	        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
	        EntitiesRid2 = searchResultId.entities;
			//System.out.println("引用了id2的论文个数为："+EntitiesRid2.size()); // 默认不大于50000
		}
		// 大小就是显示一波，没有什么用
		//System.out.println("AuId1="+AuId1+"所写论文个数为："+EntitiesAuId1.size()+"共引用论文数："+AuId1RIdNum);

		
		for(Entities entitiesAuId1 : EntitiesAuId1) //遍历作者的每一篇论文
		{
			// 尽然计算自己跳自己，是不是傻逼，尽然还是正确答案，傻逼死了
//			if(entitiesAuId1.Id.equals(id2)) // 排除作者写的论文是id2的情况
//				continue;
			
			// 判断JID 期刊 规则6，7
			//System.out.println("ID1->JId->ID2:");
			if(entitiesAuId1.J != null && entitiesId2.J != null && entitiesAuId1.J.JId.equals(entitiesId2.J.JId))
			{
				String temp = new StringBuilder(6 + AuId1.length() + entitiesAuId1.Id.length() + entitiesAuId1.J.JId.length()+ id2.length())
						.append("[").append(AuId1).append(",")
						.append(entitiesAuId1.Id).append(",")
						.append(entitiesAuId1.J.JId).append(",")
						.append(id2).append("],")
						.toString();
				result.append(temp);
				//System.out.println(temp);
			}	
			
			// 判断CID 会议 规则4，5
			//System.out.println("ID1->CId->ID2:");
			if(entitiesAuId1.C != null && entitiesId2.C != null && entitiesAuId1.C.CId.equals(entitiesId2.C.CId))
			{
				String temp = new StringBuilder(6 + AuId1.length() + entitiesAuId1.Id.length() + entitiesAuId1.C.CId.length()+ id2.length())
						.append("[").append(AuId1).append(",")
						.append(entitiesAuId1.Id).append(",")
						.append(entitiesAuId1.C.CId).append(",")
						.append(id2).append("],")
						.toString();
						result.append(temp);
						//System.out.println(temp);
			}
			
			// 判断FId 领域 规则2，3
			//System.out.println("ID1->FId->ID2:");
			if(entitiesAuId1.F != null  && entitiesId2.F != null 
			   && entitiesAuId1.F.size() != 0 && entitiesId2.F.size() != 0)
			{
				// 两个链表找出相同元素
				// 函数返回[],或者""
				String temp = IdToId_2Hop_FId(AuId1,entitiesAuId1.Id,id2,entitiesAuId1.F,entitiesId2.F);
				result.append(temp);
				//System.out.println(temp);
			}
			
			// 判断 AA.AuId 作者 规则10，11，可以不用判断，作者不可能为空
			//System.out.println("ID1->AA.AuId->ID2:");
			if(entitiesAuId1.AA != null && entitiesId2.AA != null 
				&& entitiesAuId1.AA.size() != 0 && entitiesId2.AA.size() != 0)
			{
				// 返回[],或者""
				String temp = IdToId_2Hop_AA_AuId(AuId1,entitiesAuId1.Id,id2,entitiesAuId1.AA,entitiesId2.AA);
				result.append(temp);
				//System.out.println(temp);
			}
			
			// 判断RID 参考文献 规则1 会将uri的AttributesAA.AuId;
			//System.out.println("ID1->RId->RId = ID2:");
			// 有论文应用id2,且AuId1写的论文有引用文献
			if(entitiesAuId1.RId != null && entitiesAuId1.RId.size() != 0 && EntitiesRid2.size() != 0)
			{
				String temp = IdToId_2Hop_RId(AuId1,entitiesAuId1.Id,id2,entitiesAuId1.RId,EntitiesRid2);
				result.append(temp);
				//System.out.println(temp);
			}
			
		}
		//System.out.println("3-Hop rule2 end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}
	
	/**
	 * 找到链表1和链表2相同的FID
	 * 返回[AuId1,id1,FID,id2],或者""
	 * @param list1
	 * @param list2
	 * @return
	 */
	private String IdToId_2Hop_FId(String AuId1,String id1,String id2,List<Field> list1,List<Field> list2){
		//long st = System.nanoTime();
		//System.out.println("2-Hop-FId start");
		
		StringBuilder result = new StringBuilder();
		List<Field> maxList = list1;
		List<Field> minList = list2;
		if(list2.size() > list1.size())
		{
			maxList = list2;
			minList = list1;
		}
		Map<String,Integer> map = new HashMap<String,Integer>((int)(maxList.size()/0.75));  
		for(Field field : maxList)
		{
			map.put(field.FId, 1);
		}
		
		for(Field field : minList){
			if(map.get(field.FId) != null){ // 找到了添加到结果中
				result.append("[").append(AuId1).append(",")
					.append(id1).append(",")
					.append(field.FId).append(",")
					.append(id2).append("],");
			}
		}
		
		//System.out.println("2-Hop-Fid end and total times :"+(System.nanoTime()-st));
		return result.toString();
	}
	
	/**
	 * 找到链表1和链表2相同的AuId
	 * 返回[AuId1,id1,AuIdx,id2],或者"" 
	 * 排除AuIdx是AuId1的情况
	 * @param list1
	 * @param list2
	 * @return
	 */
	private String IdToId_2Hop_AA_AuId(String AuId1,String id1,String id2,List<Author> list1,List<Author> list2){
		//long st = System.nanoTime();
		//System.out.println("2-Hop-AuId start");
		
		StringBuilder result = new StringBuilder();
		List<Author> maxList = list1;
		List<Author> minList = list2;
		if(list2.size() > list1.size())
		{
			maxList = list2;
			minList = list1;
		}
		Map<String,Integer> map = new HashMap<String,Integer>((int)(maxList.size()/0.75));  
		for(Author author : maxList)
		{
			// 作者也可以是AuId1
			// [AuId1,id1,AuId1,id2]也算是正确
//			if(author.AuId.equals(AuId1)) // 排除作者是AuId1的情况
//				continue;
			map.put(author.AuId, 1);
		}
		
		for(Author author : minList){
			if(map.get(author.AuId) != null){ // 找到了添加到结果中
				result.append("[").append(AuId1).append(",")
					.append(id1).append(",")
					.append(author.AuId).append(",")
					.append(id2).append("],");
			}
		}
		
		//System.out.println("2-Hop-AuId end and total times :"+(System.nanoTime()-st));
		return result.toString();
	}
	
	/**
	 * 找id1RId是否EntitiesRid2有没有重复的元素
	 * 返回[AuId1,id1,id1RId,id2],或""
	 * @param AuId1			作者编号AuId1
	 * @param id1			作者所写的论文id1，不可能是id2，之前已经排除
	 * @param id2			目标论文编号
	 * @param id1RId		id1引用的所有论文编号
	 * @param EntitiesRid2	所有引用id2的论文编号
	 * @return
	 */
	private String IdToId_2Hop_RId(String AuId1,String id1,String id2,List<String> id1RId,List<Entities> EntitiesRid2){
		//long st = System.nanoTime();
		//System.out.println("2-Hop-RId start");
		
		StringBuilder result = new StringBuilder();
		
		if(id1RId.size() > EntitiesRid2.size()) // id1引用的文献比引用id2的文献多
		{
			Map<String,Integer> map = new HashMap<String,Integer>((int)(id1RId.size()/0.75));  
			for(String id1rid : id1RId)
			{
				map.put(id1rid, 1);
			}
			
			for(Entities entities : EntitiesRid2){
				if(map.get(entities.Id) != null){ // 找到了添加到结果中
					result.append("[").append(AuId1).append(",")
						.append(id1).append(",")
						.append(entities.Id).append(",")
						.append(id2).append("],");
				}
			}
		}
		else
		{
			Map<String,Integer> map = new HashMap<String,Integer>((int)(EntitiesRid2.size()/0.75));  
			for(Entities entities : EntitiesRid2)
			{
				map.put(entities.Id, 1);
			}
			
			for(String id1rid : id1RId){
				if(map.get(id1rid) != null){ // 找到了添加到结果中
					result.append("[").append(AuId1).append(",")
						.append(id1).append(",")
						.append(id1rid).append(",")
						.append(id2).append("],");
				}
			}
		}
		//System.out.println("2-Hop-RId end and total times :"+(System.nanoTime()-st));
		return result.toString();
	}
}
