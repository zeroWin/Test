package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;

public class SolutionAuIdToAuId {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	public SolutionAuIdToAuId(){
		apiuse = new APIuse();
	};
	
	
	public String AuIdToAuId_All(String AuId1,String AuId2)
	{
		String result = "";

		// 差条件
		apiuse.setCount("50000"); // 个数
		apiuse.setAttributes("Id,AA.AuId,AA.AfId,RId"); // 返回的东西，根据路径分析图，只需要这四样数据
		apiuse.setOffset("0"); // 偏移
		
		// 1-hop 没有1-hop的路径
		
		// 得到AuId1需要的数据
		String expr = "Composite(AA.AuId="+AuId1+")";
		apiuse.setExpr(expr);
        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
        List<Entities> EntitiesAuId1 = searchResultId.entities;
        
        // 得到AuId2需要的数据
		expr = "Composite(AA.AuId="+AuId2+")";
		apiuse.setExpr(expr);
        searchResultId = apiuse.HandleURI(apiuse.getURI());
        List<Entities> EntitiesAuId2 = searchResultId.entities;    
        
		// 2-hop
		String path2Hop = AuIdToAuId_2Hop(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2);
		if(path2Hop.length() != 0)
			result += path2Hop;
		
		// 3-hop
		String path3Hop = AuIdToAuId_3Hop(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2);
		if(path3Hop.length() != 0)
			result += path3Hop;
		
		int t = result.length();
		if(t != 0) // 去掉最后的逗号
			return result.substring(0, result.length()-1);
		return result;
	}
	
	
	/**
	 * 用来判断规则1,2,3,4,5,6,7,10,11 找到所有AuId1到AuId2的2跳路径
	 * 1.AuId1->Id->AuId2 10,11 即两个作者是否共写了同一篇论文
	 * 2.AuId1->AfId->AuId2 8,9 即两个作者是否属于同一个组织
	 * @param AuId1
	 * @param AuId2
	 * @return [AuId1,AfId,AuId2],[AuId1,Id,AuId2]或者""
	 */
	public String AuIdToAuId_2Hop(String AuId1,String AuId2,List<Entities> EntitiesAuId1,List<Entities> EntitiesAuId2)
	{
		long st = System.nanoTime();
		String result = "";
		
		// 1.AuId1->Id->AuId2 10,11 即两个作者是否共写了同一篇论文 不可能为空，因为有作者必定写了论
		System.out.println("2-Hop-AuId1->Id->AuId2 start");
		String temp = AuIdToAuId_2Hop_Rule1(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2);
		result += temp;
		System.out.println(temp);

		
		// 2.AuId1->AfId->AuId2 8,9 即两个作者是否属于同一个组织 // 组织不唯一
		System.out.println("2-Hop-AuId1->AfId->AuId2 start");
		temp = AuIdToAuId_2Hop_Rule2(AuId1,AuId2,EntitiesAuId1,EntitiesAuId2);
		result += temp;
		System.out.println(temp);
		
		System.out.println("2-Hop end and total times ："+(System.nanoTime()-st));
	
		return result;
	}
	

	/**
	 * 使用规则1判断有没有2跳路径，规则如下：
	 * 1.AuId1->Id->AuId2 10,11 即两个作者是否共写了同一篇论文
	 * @param AuId1
	 * @param AuId2
	 * @return [AuId1,Id,AuId2],或者""
	 */
	public String AuIdToAuId_2Hop_Rule1(String AuId1,String AuId2,List<Entities> list1,List<Entities> list2)
	{
		long st = System.nanoTime();
		String result = "";
		
		List<Entities>maxList = list1;
		List<Entities>minList = list2;
		if(list2.size() > list1.size())
		{
			maxList = list2;
			minList = list1;
		}
		Map<String,Integer> map = new HashMap<String,Integer>((int)(maxList.size()/0.75));  
		for(Entities entities : maxList)
		{
			map.put(entities.Id, 1);
		}
		
		for(Entities entities : minList){
			if(map.get(entities.Id) != null){ // 找到了
				{
				result += "["+AuId1+","+entities.Id+","+AuId2+"],";
//				System.out.println(entities.Id);
				}
			}
		}	
		
		System.out.println("2-Hop-AuId1->Id->AuId2 end and total times :"+(System.nanoTime()-st));
		return result;
	}
	
	/**
	 * 使用规则2判断有没有2跳路径，规则如下：
	 * 2.AuId1->AfId->AuId2 8,9 即两个作者是否属于同一个组织
	 * @param AuId1
	 * @param AuId2
	 * @return [AuId1,AfId,AuId2],或者""
	 */
	public String AuIdToAuId_2Hop_Rule2(String AuId1,String AuId2,List<Entities> list1,List<Entities> list2)
	{
		long st = System.nanoTime();
		String result = "";
		
		List<Entities>maxList = list1;
		List<Entities>minList = list2;
		if(list2.size() > list1.size())
		{
			maxList = list2;
			minList = list1;
		}
		Map<String,Integer> map = new HashMap<String,Integer>((int)(maxList.size()/0.75));
		System.out.println("AuId1="+AuId1+"所写论文个数为："+list1.size());
		System.out.println("AuId2="+AuId2+"所写论文个数为："+list2.size());
		for(Entities entities : maxList)	// 将作者1所有的可能在的领域领域放到图中
		{
			for(Author author : entities.AA ) //遍历作者
			{
				if(author.AuId.equals(AuId1)) //该作者是AuId1
				{
					if(author.AfId != null)	//有所属领域
					{
						System.out.println(author.AfId);
						if(map.get(author.AfId) == null)// 如果map中没有加入到map中
							map.put(author.AfId, 1); // 把AfId加入到map中
					}
					break;	//跳出此次循环
				}
			}

		}
		if(map.isEmpty())	//图是空直接返回
		{
			System.out.println("AuId1不属于任何一个组织 2-Hop-AuId1->AfId->AuId2 end and total times :"+(System.nanoTime()-st));
			return result;
		}
		System.out.println("AuId1="+AuId1+"会归属于"+map.size()+"个组织");
		
		// 找作者2在这些领域中吗
		for(Entities entities : minList)
		{
			for(Author author : entities.AA) // 遍历作者
			{
				if(author.AuId.equals(AuId2)) //该作者是AuId2
				{
					if(author.AfId != null)
					{
						if(map.get(author.AfId) != null) //map中找到了
						{
							result += "["+AuId1+","+author.AfId+","+AuId2+"]"+",";
							map.remove(author.AfId);	//移除找到的键值
							if(map.isEmpty())	// map 空了直接返回
							{
								System.out.println("已找到所有组织路径 2-Hop-AuId1->AfId->AuId2 end and total times :"+(System.nanoTime()-st));
								return result;
							}
						}
					}
					
					break;
				}
			}
		}	
		
		System.out.println("2-Hop-AuId1->AfId->AuId2 end and total times :"+(System.nanoTime()-st));
		return result;
	}
	
	/**
	 * 用来判断规则1,10,11 找到所有AuId1到AuId2的3跳路径
	 * 3跳路径只有下面一种情况
	 * 1.AuId1->Id->RId(Id)->AuId2
	 * 思路1：作者1的的论文引用文献的作者是AuId2
	 * 思路2：找作者为作者1，且引用了作者2写的论文
	 * @param AuId1
	 * @param AuId2
	 * @return [AuId1,Id,RId,AuId2]或者""
	 */
	public String AuIdToAuId_3Hop(String AuId1,String AuId2,List<Entities> EntitiesAuId1,List<Entities> EntitiesAuId2)
	{
		long st = System.nanoTime();
		String result = "";
		
		// 先计算作者1所有论文共引用了多少篇论文方便给hashmap开辟空间
		long AuIdPaperRidNum = 0;
		for(Entities entities : EntitiesAuId1)
			AuIdPaperRidNum += entities.RId.size();
		
		System.out.println("AuId1="+AuId1+"所写论文引用其他论文个数为："+AuIdPaperRidNum);
		if(AuIdPaperRidNum == 0)
			return result = "";
		
		// 设定key为RId，value为Id
		Map<String,String> map = new HashMap<String,String>((int)(AuIdPaperRidNum/0.75));
		for(Entities entities : EntitiesAuId1) // 遍历AuId1的每一个实体
		{
			if(entities.RId.size() == 0) // 没有引用文献
				continue;
			
			for(String auid1Rid : entities.RId) // 遍历所有引用文献,并将值存成应用文献对应的id
				map.put(auid1Rid, entities.Id);
		}
		
		for(Entities entities : EntitiesAuId2)	// 遍历AuId2的每一个实体
		{
			String getId = map.get(entities.Id);
			if(getId != null) // 找到了
			{
				//			 AuId1     Id          RId			AuId2
				result += "["+AuId1+","+getId+","+entities.Id+","+AuId2+"],";
			}
		}
		
		System.out.println("3-Hop end and total times ："+(System.nanoTime()-st));
		
		return result;
	}	
	
	
	/**
	 * 给字符串两边加Or
	 * @param string1
	 * @param string2
	 * @return Or(string1,string2)
	 */
	public String addOr(String string1,String string2){
		return "Or("+string1+","+string2+")";
	}
	
	/**
	 * 给字符串两边加And
	 * @param string1
	 * @param string2
	 * @return And(string1,string2)
	 */
	public String addAnd(String string1,String string2){
		return "And("+string1+","+string2+")";
	}
	
}
