package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.ResultJsonClass.Field;

public final class SolutionIdToAuId {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	public SolutionIdToAuId(){
		apiuse = new APIuse();
	};
	
	// 总共三次搜索完成所有操作
	public String IdToAuId_All(String Id1,String AuId2,Entities EntitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("Find start");
//		String result = null;
		
		// 1-hop 1-hop的路径"" 或 [],
		String path1Hop = IdToAuId_1Hop(Id1,AuId2,EntitiesId1);

		// 2-hop 返回""或者[],
		String path2Hop = IdToAuId_2Hop(Id1,AuId2,EntitiesId1,EntitiesAuId2);
		
		// 3-hop 返回""着这[],
		String path3Hop = IdToAuId_3Hop(Id1,AuId2,EntitiesId1,EntitiesAuId2);

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
	 * 用来判断1-hop，Id1 -> Auid2的一跳路径 只有1种情况
	 * 1.id1这篇论文的作者是AuId2
	 * @param Auid1
	 * @param id2
	 * @return [Id1,AuId2], 或者 ""
	 */
	public String IdToAuId_1Hop(String Id1,String AuId2,Entities entitiesId1)
	{
		//long st = System.nanoTime();
		//System.out.println("1-Hop start");
		String result = null;
		
		List<Author> entitiesId1AATemp = entitiesId1.AA;
		for(Author author : entitiesId1AATemp)
		{
			if(author.AuId.equals(AuId2)) // 找到了作者是AuId2
			{
				result = concatString("[",Id1,",",AuId2,"],","");
				//System.out.println(result);
				break;
			}
		}
		
		//System.out.println("1-Hop end and total times ："+(System.nanoTime()-st));	
		if(result == null)
			return "";
		return result;	
	}

	/**
	 * 用来判断2-hop，Id1 -> AuId2的2跳路径 只有1种情况
	 * 1.Id1->RId->AuId2  即id1引用文献的作者是AuId2
	 * 换个思路：就是作者为AuId2的论文是id1的引用文献
	 * @param Auid1
	 * @param id2
	 * @return [Id1,RId(id),AuId2)], 或者 ""
	 */
	public String IdToAuId_2Hop(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("2-Hop start");
		StringBuilder result = new StringBuilder();
		
		long Id1RIdNum = entitiesId1.RId.size(); // Id1参考文献数目
		long AuId2PaperNum = EntitiesAuId2.size();	// AuId2写的论文数
		
		if(Id1RIdNum == 0) //论文1没有参考文献,直接退出
		{}
		else
		{
			if(Id1RIdNum > AuId2PaperNum) // id1参考文献数目大于AuId2写的论文数
			{
				Map<String,Integer> map = new HashMap<String,Integer>((int)(Id1RIdNum/0.75));
				List<String> entitiesId1RIdTemp = entitiesId1.RId;
				for(String everyRId : entitiesId1RIdTemp)
					map.put(everyRId, 1);
				
				for(Entities entities:EntitiesAuId2) // 遍历Id2的每一篇论文
				{
					if(map.get(entities.Id) != null) // 找到了
					{
						result.append("[")
						.append(Id1).append(",")
						.append(entities.Id).append(",")
						.append(AuId2).append("],");						
					}
				}
			}
			else // AuId2写的论文数大于id2的参考文献数目
			{
				Map<String,Integer> map = new HashMap<String,Integer>((int)(AuId2PaperNum/0.75));
				for(Entities entities:EntitiesAuId2) // 遍历AuId2的每一篇论文
					map.put(entities.Id, 1);
				
				List<String> entitiesId1RIdTemp = entitiesId1.RId;
				for(String everyRId : entitiesId1RIdTemp) // 遍历每一篇参考文献
				{
					if(map.get(everyRId) != null) // 找到了
					{
						result.append("[")
						.append(Id1).append(",")
						.append(everyRId).append(",")
						.append(AuId2).append("],");						
					}
				}				
			}
		}
		
		//System.out.println("Id1="+Id1+"引用论文个数为"+Id1RIdNum);
		//System.out.println("AuId2="+AuId2+"所写论文个数为"+AuId2PaperNum);
		//System.out.println("2-Hop end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}
	
	
	
	/**
	 * 用来判断3-hop，Id1 -> Auid2的3跳路径 5种情况
	 * 1.id1->AuId->Idx->AuId2 即作者2写的论文里也是id1的作者写的论文
	 * 换一个思路：找Idx有没有作者是id1的作者
	 * 2.id1->AuId->AfId->AuId2 即论文1作者的所属组织与auid2所属组织相同
	 * 3.id1->CId->Idx->AuId2 即作者2写的论文和id1是在同一个会议
	 * 4.id1->JId->Idx->AuId2 即作者2写的论文和id1是在同一个期刊
	 * 5.id1-FId->Idx->AuId2 即作者2写的论文和id1在同一个领域
	 * 6.id1->RId->RId->AuId2 即id1引用文献的引用文献作者是AuId2
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,AuId,Id,AuId2],
	 * [Id1,C.CId,Id,AuId2],
	 * [Id1,F.FId,Id,AuId2],
	 * [Id1,J.JId,Id,AuId2],
	 * [Id1,RID,RID,AuId2],
	 *  或者 ""
	 */
	public String IdToAuId_3Hop(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop start");
		String result = null;
		
		// 1.id1->AuId->Idx->AuId2 即作者2写的论文里也是id1的作者写的论文
		// 返回[],或者""
		String result1 = IdToAuId_3Hop_rule1(Id1,AuId2,entitiesId1,EntitiesAuId2);

		// 2.id1->AuId->AfId->AuId2 即论文1作者的所属组织与auid2所属组织相同
		// 返回[],或者""
		String result2 = IdToAuId_3Hop_rule2(Id1,AuId2,entitiesId1,EntitiesAuId2);
		
		// 3.id1->CId->Idx->AuId2 即作者2写的论文和id1是在同一个会议
		// 返回[],或者""
		String result3 = IdToAuId_3Hop_rule3(Id1,AuId2,entitiesId1,EntitiesAuId2);
		
		// 4.id1->JId->Idx->AuId2 即作者2写的论文和id1是在同一个期刊
		// 返回[],或者""
		String result4 = IdToAuId_3Hop_rule4(Id1,AuId2,entitiesId1,EntitiesAuId2);		
		
		// 5.id1-FId->Idx->AuId2 即作者2写的论文和id1在同一个领域
		// 返回[],或者""
		String result5 = IdToAuId_3Hop_rule5(Id1,AuId2,entitiesId1,EntitiesAuId2);
		
		// 6.id1->RId->RId->AuId2 即id1引用文献的引用文献作者是AuId2
		// 返回[],或者""
		String result6 = IdToAuId_3Hop_rule6(Id1,AuId2,entitiesId1,EntitiesAuId2);
		
		result = new StringBuilder(result1.length()+result2.length()+result3.length()+result4.length()+result5.length()+result6.length())
				.append(result1).append(result2).append(result3).append(result4).append(result5).append(result6).toString();
		//System.out.println("3-Hop end and total times ："+(System.nanoTime()-st));
		return result.toString();
		
		
	}	
	
	
	/**
	 * 用来判断3-hop的规则1
	 * 1.id1->AuId->Idx->AuId2 即作者2写的论文里也是id1的作者写的论文
	 * 换一个思路：找Idx有没有作者是id1的作者
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,AuId,Id,AuId2],或者 ""
	 */
	public String IdToAuId_3Hop_rule1(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule1 start");
		StringBuilder result = new StringBuilder();
	
		long Id1AuIdNum = entitiesId1.AA.size();
		//System.out.println("id1="+Id1+"作者个数为："+Id1AuIdNum);
		
		Map<String,Integer> map = new HashMap<String,Integer>((int)(Id1AuIdNum/0.75));
		List<Author> entitiesAAId1temp = entitiesId1.AA;
		for(Author author : entitiesAAId1temp) // 存放所有作者
			map.put(author.AuId, 1);
		
		for(Entities entities : EntitiesAuId2) // 遍历作者2写的每一篇论文
		{
			String TempId = entities.Id;
			List<Author> entitiesAAtemp = entities.AA;
			for(Author author : entitiesAAtemp) // 遍历每一个作者
			{
				if(map.get(author.AuId) != null) // 找到了
					result.append("[")
					.append(Id1).append(",")
					.append(author.AuId).append(",")
					.append(TempId).append(",")
					.append(AuId2).append("],");				
			}
		}
		
		//System.out.println("3-Hop rule1 end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}		

	
	/**
	 * 用来判断3-hop的规则2
	 * 2.id1->AuId->AfId->AuId2 即论文1作者的所属组织与auid2所属组织相同
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,AuId,AfId,AuId2],或者 ""
	 */
	public String IdToAuId_3Hop_rule2(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule2 start");
		StringBuilder result = new StringBuilder();
	
		
		// 先找AuId2所有可能属于的组织，这里的大小就是显示一波，没什么用
		Map<String,Integer> map = new HashMap<String,Integer>((int)(EntitiesAuId2.size()/0.75));
		//System.out.println("AuId2="+AuId2+"所写论文个数为："+EntitiesAuId2.size());
		//System.out.println("Id1="+Id1+"作者个数为："+entitiesId1.AA.size());
		for(Entities entities : EntitiesAuId2)	// 将作者1所有的可能在的领域领域放到图中
		{
			List<Author> entitiesAAtemp = entities.AA;
			for(Author author : entitiesAAtemp ) //遍历作者
			{
				if(author.AuId.equals(AuId2)) //该作者是AuId2
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
			//System.out.println("AuId2不属于任何一个组织 3-Hop-Rule2 end and total times :"+(System.nanoTime()-st));
			return "";
		}
		
		// 找id2的作者中有图中组织的，没找出id2的作者所有可能在的组织，但先放放，先搞其他的
		List<Author> entitiesId1AATemp = entitiesId1.AA;
		for(Author author : entitiesId1AATemp) // 遍历每一个作者
		{
//			if(author.AuId.equals(AuId2)) // 排除到论文作者是AuId2的情况
//				continue;
			
			if(map.get(author.AfId) != null) // 这个作者在图中找到了组织，存在路径
				result.append("[")
					.append(Id1).append(",")
					.append(author.AuId).append(",")
					.append(author.AfId).append(",")
					.append(AuId2).append("],");
		}
		
		//System.out.println("AuId2="+AuId2+"会归属于"+map.size()+"个组织");		
		//System.out.println("3-Hop rule2 end and total times ："+(System.nanoTime()-st));
		return result.toString();		
	}		
	
	/**
	 * 用来判断3-hop的规则3
	 * 3.Id1->CId->Idx->AuId2 即作者2写的论文和id1是在同一个会议
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,CId,Id,AuId2],或者 ""
	 */
	public String IdToAuId_3Hop_rule3(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule3 start");
		StringBuilder result = new StringBuilder();
	
		if(entitiesId1.C != null) // id1有CId才做处理
		{
			String Id1CId = entitiesId1.C.CId;
			for(Entities entities : EntitiesAuId2) // 遍历作者2写的每一篇论文
			{
				if(entities.C != null && entities.C.CId.equals(Id1CId)) // 找到了相同的CId
					result.append("[")
					.append(Id1).append(",")
					.append(Id1CId).append(",")
					.append(entities.Id).append(",")
					.append(AuId2).append("],");						
			}
		}
		
		//System.out.println("3-Hop rule3 end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}		
	
	/**
	 * 用来判断3-hop的规则4
	 * 4.Id1->JId->Idx->AuId2 即作者2写的论文和id1是在同一个期刊
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,JId,Id,AuId2],或者 ""
	 */
	public String IdToAuId_3Hop_rule4(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule4 start");
		StringBuilder result = new StringBuilder();
	
		if(entitiesId1.J != null) // id1有JId才做处理
		{
			String Id1JId = entitiesId1.J.JId;
			for(Entities entities : EntitiesAuId2) // 遍历作者2写的每一篇论文
			{
				if(entities.J != null && entities.J.JId.equals(Id1JId)) // 找到了相同的JId
					result.append("[")
					.append(Id1).append(",")
					.append(Id1JId).append(",")
					.append(entities.Id).append(",")
					.append(AuId2).append("],");						
			}
		}
		
		//System.out.println("3-Hop rule4 end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}		
	
	/**
	 * 用来判断3-hop的规则5
	 * 5.Id1->FId->Idx->AuId2 即作者2写的论文和id1是在同一个领域
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,FId,Id,AuId2],或者 ""
	 */
	public String IdToAuId_3Hop_rule5(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule5 start");
		StringBuilder result = new StringBuilder();
	
		long Id1FIdNum = entitiesId1.F.size();
		//System.out.println("id1="+Id1+"所属领域个数为："+ Id1FIdNum);
		if(Id1FIdNum != 0) // id1有FId才做处理
		{
			Map<String,Integer> map = new HashMap<String,Integer>((int)(Id1FIdNum/0.75));
			List<Field> entitieId1FId = entitiesId1.F;
			for(Field field  : entitieId1FId) // 存放所有作者
				map.put(field.FId, 1);
			
			for(Entities entities : EntitiesAuId2) // 遍历作者2写的每一篇论文
			{
				List<Field> entitieFId = entities.F;
				String TempId = entities.Id;
				for(Field field : entitieFId) // 遍历每一个论文的FId
				{
					if(map.get(field.FId)!= null ) // 找到了相同的FId
						result.append("[")
						.append(Id1).append(",")
						.append(field.FId).append(",")
						.append(TempId).append(",")
						.append(AuId2).append("],");	
				}
			}
		}
		
		//System.out.println("3-Hop rule5 end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}		
	
	/**
	 * 用来判断3-hop的规则6
	 * 6.id1->RId->RId->AuId2 即id1引用文献的引用文献作者是AuId2
	 * @param Auid1
	 * @param id2
	 * @return 
	 * [Id1,RId,RId,AuId2],或者 ""
	 */
	public String IdToAuId_3Hop_rule6(String Id1,String AuId2,Entities entitiesId1,List<Entities> EntitiesAuId2)
	{
		//long st = System.nanoTime();
		//System.out.println("3-Hop rule6 start");
		StringBuilder result = new StringBuilder();
	
		// Id1有参考文献才做处理
		long Id1RIdNum = entitiesId1.RId.size(); // Id1参考文献数目
		long AuId2PaperNum = EntitiesAuId2.size();	// AuId2写的论文数
		if(Id1RIdNum == 0 ) // Id1没有参考文献，直接退出
		{}
		else // Id1有参考文献，进入处理
		{
			// 先将作者AuId2写的论文放入到图中
			Map<String,Integer> map = new HashMap<String,Integer>((int)(AuId2PaperNum/0.75));
			for(Entities entitiesAuId2 : EntitiesAuId2)
				map.put(entitiesAuId2.Id, 1);
				
			// 所搜Id1所有RId的RId
			StringBuilder expr = new StringBuilder();
			int flag = 0;
			// 设定要求返回的内容
			ResultJsonClass searchResult;
			List<Entities> searchEntities;
			List<String> EntitiesRId;
			apiuse.setAttributes("RId");
			List<String> EntitiesId1RId = entitiesId1.RId;
			for(String Id1RId: EntitiesId1RId) // 遍历每一个RId
			{
				if(flag == 0)
				{
					expr.append("Id=").append(Id1RId);
					flag++;
				}
				else
				{
					expr.insert(0, "Or(").append(",Id=")
						.append(Id1RId).append(")");
					flag++;
				}
				if(flag == 65) // 足够长了，搜索一次
				{
					apiuse.setExpr(expr.toString());
					searchResult = apiuse.HandleURI(apiuse.getURI());
					searchEntities = searchResult.entities;
					//if(searchEntities.size() != flag)
						//System.out.println("注意了：这里的Or搜索有问题,结果个数和设定个数对不上！！！！！！");
					
					for(Entities entities : searchEntities)	// 遍历搜索到的每一篇论文的RId
					{
						EntitiesRId = entities.RId;
						for(String everyRId : EntitiesRId)
						{
							if(map.get(everyRId) != null) //找到了[Id1,RId,RId,AuId2],
								result.append("[").append(Id1).append(",")
									.append(entities.Id).append(",")
									.append(everyRId).append(",")
									.append(AuId2).append("],");
						}
					}
					expr = new StringBuilder();
					flag = 0;
				}
			}
			if(flag != 0) // 还差最后一次搜索
			{
				apiuse.setExpr(expr.toString());
				searchResult = apiuse.HandleURI(apiuse.getURI());
				searchEntities = searchResult.entities;
				//if(searchEntities.size() != flag)
					//System.out.println("注意了：这里的Or搜索有问题,结果个数和设定个数对不上！！！！！！");
				
				for(Entities entities : searchEntities)	// 遍历搜索到的每一篇论文的RId
				{
					EntitiesRId = entities.RId;
					for(String everyRId : EntitiesRId)
					{
						if(map.get(everyRId) != null) //找到了[Id1,RId,RId,AuId2],
							result.append("[").append(Id1).append(",")
								.append(entities.Id).append(",")
								.append(everyRId).append(",")
								.append(AuId2).append("],");
					}
				}
			}
			
		}
		
		//System.out.println("Id1="+Id1+"引用论文个数为"+Id1RIdNum);
		//System.out.println("AuId2="+AuId2+"所写论文个数为"+AuId2PaperNum);		
		//System.out.println("3-Hop rule6 end and total times ："+(System.nanoTime()-st));
		return result.toString();		
	}
		
	// 高效率拼接字符串用
    public static String concatString(String s1, String s2, String s3, String s4, String s5, String s6) {
      return new StringBuilder(s1.length() + s2.length() + s3.length() + s4.length() + s5.length() + s6.length())
              .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString();

  }
    
}
