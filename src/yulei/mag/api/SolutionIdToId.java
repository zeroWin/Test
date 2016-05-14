package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.ResultJsonClass.Field;

public class SolutionIdToId {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	public SolutionIdToId(){
		apiuse = new APIuse();
		// 初始化搜索所有引用id2的论文
		apiuse.setCount("50000"); // 个数
		// 返回的东西，根据路径分析图，没有必要知道引用了id2的论文引用了什么其他的论文，只需要知道引用了id2即可
		apiuse.setAttributes("Id,C.CId,F.FId,J.JId,AA.AuId"); 
		apiuse.setOffset("0"); // 偏移
	};

	// 大概要搜三次+id1RId/60
	public String IdToId_All(String id1,String id2,Entities EntitiesId1,Entities EntitiesId2)
	{
		//long st = System.nanoTime();
		//System.out.println("Find start");
		String result = null;


		String exprId1 = new StringBuilder(4+id1.length())
				.append("RId=").append(id2).toString();
		apiuse.setExpr(exprId1);
		
		ResultJsonClass searchResultRId2 = apiuse.HandleURI(apiuse.getURI());
		
		// 1-hop
		String path1Hop = IdToId_1Hop(id1, id2,EntitiesId1);
		
		// 2-hop
		String path2Hop = IdToId_2Hop(id1, id2,EntitiesId1,EntitiesId2,searchResultRId2.entities);

		// 3-hop
		String path3Hop = IdToId_3Hop(id1,id2,EntitiesId1,EntitiesId2,searchResultRId2.entities);
	
		result = new StringBuilder(path1Hop.length()+path2Hop.length()+path3Hop.length())
				.append(path1Hop).append(path2Hop).append(path3Hop).toString();
		
		//System.out.println("Find end and total times ："+(System.nanoTime()-st));			
		int t = result.length();
		if(t != 0) // 去掉最后的逗号
			return result.substring(0, result.length()-1);
		return result;
	}
	
	/**
	 * 用来判断1-hop，id1.Rid = id2，找到所有id到id的一跳路径
	 * @param id1
	 * @param id2
	 * @return [1,2], 或者 ""
	 */
	public String IdToId_1Hop(String id1,String id2,Entities EntitiesId1){
		//System.out.println("1-Hop start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		
		for(String Id1RId : EntitiesId1.RId)
		{
			if(Id1RId.equals(id2)) // 找到了
			{
				result.append("[").append(id1).append(",").append(id2).append("],");
				break;
			}
		}
		//System.out.println("1-Hop end and total times :"+(System.nanoTime()-st));  
		
        // 路径存在
        return result.toString();
	}
	
	/**
	 * 用来判断规则1,2,3,4,5,6,7,10,11 找到所有id到id的2跳路径
	 * 1.id1.RId.RId = id2? 1
	 * 2.id1.F.Fid = id2.F.Fid? 2,3
	 * 3.id1.J.Jid = id2.J.Jid? 4,5
	 * 4.id1.C.Cid = id2.C.Cid? 6,7
	 * 5.id1.AA.AuId = id2.AA.AuId? 10,11
	 * @param id1
	 * @param id2
	 * @return [1,2],[3,4],[5,6] 或者""
	 */
	public String IdToId_2Hop(String id1,String id2,Entities EntitiesId1,Entities EntitiesId2,List<Entities> PaperRefId2){
		//System.out.println("2-Hop start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		int id1Length = id1.length();
		int id2Length = id2.length();
		// 判断JID 期刊 规则6，7
		//System.out.println("ID1->JId->ID2:");
		if(EntitiesId1.J != null && EntitiesId2.J != null && EntitiesId1.J.JId.equals(EntitiesId2.J.JId))
		{
			StringBuilder temp = new StringBuilder(5+EntitiesId1.J.JId.length()+id1Length+id2Length)
					.append("[").append(id1).append(",").append(EntitiesId1.J.JId).append(",")
					.append(id2).append("],");
			result.append(temp);
			//System.out.println(temp);
		}	
		
		// 判断CID 会议 规则4，5
		//System.out.println("ID1->CId->ID2:");
		if(EntitiesId1.C != null && EntitiesId2.C != null && EntitiesId1.C.CId.equals(EntitiesId2.C.CId))
		{
			StringBuilder temp = new StringBuilder(5+EntitiesId1.C.CId.length()+id1Length+id2Length)
					.append("[").append(id1).append(",").append(EntitiesId1.C.CId).append(",")
					.append(id2).append("],");
			result.append(temp);
			//System.out.println(temp);
		}
		
		// 判断FId 领域 规则2，3
		//System.out.println("ID1->FId->ID2:");
		if(EntitiesId1.F != null  && EntitiesId2.F != null 
		   && EntitiesId1.F.size() != 0 && EntitiesId2.F.size() != 0)
		{
			// 两个链表找出相同元素
			String temp = IdToId_2Hop_FId(id1,id2,EntitiesId1.F,EntitiesId2.F);
			result.append(temp);
			//System.out.println(temp);
		}
        
	
		// 判断 AA.AuId 作者 规则10，11
		//System.out.println("ID1->AA.AuId->ID2:");
		if(EntitiesId1.AA != null && EntitiesId2.AA != null 
			&& EntitiesId1.AA.size() != 0 && EntitiesId2.AA.size() != 0)
		{
			String temp = IdToId_2Hop_AA_AuId(id1,id2,EntitiesId1.AA,EntitiesId2.AA);
			result.append(temp);
			//System.out.println(temp);
		}
		
		// 判断RID 参考文献 规则1 会将uri的AttributesAA.AuId;
		//System.out.println("ID1->RId->RId = ID2:");
		if(EntitiesId1.RId != null && EntitiesId1.RId.size() != 0 && PaperRefId2.size() != 0)
		{ // Id1有参考文献且有文章引用Id2
			String temp = IdToId_2Hop_RId(id1,id2,EntitiesId1.RId,PaperRefId2);
			result.append(temp);
			//System.out.println(temp);
		}
		
		//System.out.println("2-Hop end and total times :"+(System.nanoTime()-st));
		
		// 返回结果
		return result.toString();
		
	}
	

	/**
	 * 找到链表1和链表2相同的FID
	 * @param list1
	 * @param list2
	 * @return
	 */
	private String IdToId_2Hop_FId(String id1,String id2,List<Field> list1,List<Field> list2){
		//System.out.println("2-Hop-FId start");
		//long st = System.nanoTime();
		
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
			if(map.get(field.FId) != null){ // 
				result.append("[").append(id1).append(",").append(field.FId).append(",").append(id2).append("],");
			}
		}
		
		//System.out.println("2-Hop-Fid end and total times :"+(System.nanoTime()-st));
		return result.toString();
	}
	
	
	/**
	 * 找到链表1和链表2相同的AA.AuId
	 * @param list1
	 * @param list2
	 * @return
	 */
	private String IdToId_2Hop_AA_AuId(String id1,String id2,List<Author> list1,List<Author> list2){
		//long st = System.nanoTime();
		//System.out.println("2-Hop-AA.AuId start");
		
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
			map.put(author.AuId, 1);
		}
		
		for(Author author : minList){
			if(map.get(author.AuId) != null){ // 找到了
				result.append("[").append(id1).append(",").append(author.AuId).append(",").append("],");
			}
		}
		
		//System.out.println("2-Hop-AA.AuId end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}
	
	
	/**
	 * 找到链表1的RId的Rid是id2，及id1的参考文献的参考文献中有id2
	 * 思路：RId=id2的论文，找这些论文里有没有id1的RId 这样只用搜一次
	 * @param id1 id1的编号
	 * @param id2 id2的编号
	 * @param list1 id1的所有Rid
	 * @return [id1,RId,id2],或""
	 * 进到这个函数，说明有id1引用了文章且有文章引用id2
	 */
	private String IdToId_2Hop_RId(String id1,String id2,List<String> Id1RId,List<Entities> PaperRefId2){
		//System.out.println("2-Hop-RId start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		
		int Id1RIdNum = Id1RId.size();
		int PaperRefId2Num = PaperRefId2.size();
		Map<String,Integer> map;
		
		if(Id1RId.size() > PaperRefId2.size()) // id1引用的文献比引用id2的文献多
		{
			map = new HashMap<String,Integer>((int)(Id1RIdNum/0.75)); 
			for(String id1RId : Id1RId) // 把所有引用文献放入hash表中
				map.put(id1RId, 1);
			
			for(Entities entities : PaperRefId2)
			{
				if(map.get(entities.Id) != null )// 找到了
					result.append("[").append(id1).append(",").append(entities.Id).append(",").append(id2).append("],");
			}
		}
		else //id1引用的文献比引用id2的文献少
		{
			map = new HashMap<String,Integer>((int)(PaperRefId2Num/0.75)); 
			for(Entities entities : PaperRefId2)
				map.put(entities.Id, 1);
			
			for(String id1RId : Id1RId)
			{
				if(map.get(id1RId) != null) //找到了
					result.append("[").append(id1).append(",").append(id1RId).append(",").append(id2).append("],");
			}
			
		}
		//System.out.println("Id1="+id1+"引用其他论文个数为："+Id1RIdNum);
		//System.out.println("其中引用id2的论文篇数为："+ PaperRefId2Num);
		//System.out.println("2-Hop-RId end and total times ："+(System.nanoTime()-st));
		return result.toString();
	}
	
	
	/* 下面代码为3-hop 判断相关代码 */
	/**
	 * 用来判断所有规则 找到所有id1到id2的3跳路径
	 * 1.id1->id1.RId 找所有id1.RId到id2的2跳路径 1
	 * 2.id1->id1.JId->找RId为id2且属于JId的所有论文  6，7
	 * 3.id1->id1.CId->找RId为id2且属于CId的所有论文 4，5
	 * 4.id1->id1.FId->找RId为id2且属于FId的所有论文 2，3
	 * 5.id1->id1.AuId->找RId为id2且作者为AuId的所有论文 10，11
	 * @param id1
	 * @param id2
	 * @return
	 */
	public String IdToId_3Hop(String id1,String id2,Entities EntitiesId1,Entities EntitiesId2,List<Entities> PaperRefId2){
		//System.out.println("3-Hop start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		int PaperRefId2Num = PaperRefId2.size();
		// 1.id1->id1.RId 找所有id1.RId到id2的2跳路径
		if(EntitiesId1.RId != null && EntitiesId1.RId.size() != 0)
		{
			result.append(IdToId_3Hop_Rule1(id1,id2,EntitiesId1.RId,EntitiesId2,PaperRefId2));
		}
		
		if(PaperRefId2Num == 0) // 没有文章引用id2，下面的都不用找了
		{}
		else
		{
			// 2.id1->id1.JId->找RId为id2且属于JId的所有论文
			if(EntitiesId1.J != null) // id1有发表在期刊上，且有文章引用Id2
			{
				result.append(IdToId_3Hop_Rule2(id1,id2,EntitiesId1.J.JId,PaperRefId2));
			}
			
			// 3.id1->id1.CId->找RId为id2且属于CId的所有论文
			if(EntitiesId1.C != null) // id1有发表在会议上，且有文章引用id2
			{
				result.append(IdToId_3Hop_Rule3(id1,id2,EntitiesId1.C.CId,PaperRefId2));
			}
			
			// 4.id1->id1.FId->找RId为id2且属于FId的所有论文
			if(EntitiesId1.F != null && EntitiesId1.F.size() != 0 ) // id1有所属领域，且有文章引用id2
			{
				result.append(IdToId_3Hop_Rule4(id1,id2,EntitiesId1.F,PaperRefId2));
			}
			
			// 5.id1->id1.AuId->找RId为id2且作者为AuId的所有论文
			if(EntitiesId1.AA != null && EntitiesId1.AA.size() != 0)
			{
				result.append(IdToId_3Hop_Rule5(id1,id2,EntitiesId1.AA,PaperRefId2));
			}
		}
		//System.out.println("3-Hop end and total times ："+(System.nanoTime()-st));
		// 返回结果
		return result.toString();
	}
	
	/**
	 * 使用规则1判断是否有3跳路径
	 * 规则1：id1->id1.RId 找所有id1.RId到id2的2跳路径
	 * @return [id1,RId,x,Id2], x可能是ID,JId,FId,CId,AuId或者 ""
	 * 进来了说明id1肯定有引用文献
	 */
	public String IdToId_3Hop_Rule1(String id1,String id2,List<String> ID1_RIdlist,Entities EntitiesId2,List<Entities> PaperRefId2){
		//System.out.println("3-Hop-Rule1 start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		StringBuilder expr = new StringBuilder();
		int flag = 0;
		
		// 设定要求返回的内容
		ResultJsonClass searchResult;
		List<Entities> searchEntities;
		apiuse.setAttributes("Id,C.CId,F.FId,J.JId,AA.AuId,RId");
		for(String Id1RId: ID1_RIdlist) // 遍历每一个RId
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
			if(flag == 50) // 足够长了，搜索一次
			{
				apiuse.setExpr(expr.toString());
				searchResult = apiuse.HandleURI(apiuse.getURI());
				searchEntities = searchResult.entities;
				//if(searchEntities.size() != flag)
					//System.out.println("注意了：这里的Or搜索有问题,结果个数和设定个数对不上！！！！！！");
				
				
				for(Entities entities : searchEntities)	// 对搜索到的每一个论文找2跳路径
				{
					//System.out.println("tttttt"+entities.Id);
					// 返回结果是[RId,X,id2],
					result.append(IdToId_2Hop(entities.Id, id2, entities, EntitiesId2, PaperRefId2));
				}
				flag = 0;
			}
		}
		if(flag != 0) // 还差最后一次搜索
		{
			apiuse.setExpr(expr.toString());
			//System.out.println(expr.toString());
			searchResult = apiuse.HandleURI(apiuse.getURI());
			searchEntities = searchResult.entities;
			//if(searchEntities.size() != flag)
				//System.out.println("注意了：这里的Or搜索有问题,结果个数和设定个数对不上！！！！！！");
			
			
			for(Entities entities : searchEntities)	// 对搜索到的每一个论文找2跳路径
			{
				//System.out.println("tttttt"+entities.Id);
				// 返回结果是[RId,X,id2],
				result.append(IdToId_2Hop(entities.Id, id2, entities, EntitiesId2, PaperRefId2));
			}			
		}		
		
		String resultTemp = result.toString().replace("[", "["+id1+",");
		//System.out.println("3-Hop-Rule1 end and total times ："+(System.nanoTime()-st));	
		return resultTemp;
	}
	
	/**
	 * 使用规则2判断是否有3跳路径
	 * 规则2：id1->id1.JId->找RId为id2且属于JId的所有论文
	 * 进来了说明id1有JId且有文章引用id2
	 * @return [id1,JId,idx,id2], 或者 ""
	 */
	public String IdToId_3Hop_Rule2(String id1,String id2,String JId,List<Entities> PaperRefId2){
		//System.out.println("3-Hop-Rule2 start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		
		for(Entities entities : PaperRefId2) // 遍历每一个引用id2的实体
		{
			if(entities.J != null && entities.J.JId.equals(JId)) // 找到了
				result.append("[").append(id1).append(",").append(JId).append(",").append(entities.Id).append(",").append(id2).append("],");
		}
		
		//System.out.println("3-Hop-Rule2 end and total times ："+(System.nanoTime()-st));	
		return result.toString();
	}

	/**
	 * 使用规则3判断是否有3跳路径
	 * 规则3.id1->id1.CId->找出会议是CId且引用了RId的所有论文
	 * 进来了说明id1有CId且有文章引用id2
	 * @return [id1,CId,idx,id2], 或者 ""
	 */
	public String IdToId_3Hop_Rule3(String id1,String id2,String CId,List<Entities> PaperRefId2){
		//System.out.println("3-Hop-Rule3 start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		
		for(Entities entities : PaperRefId2) // 遍历每一个引用id2的实体
		{
			if(entities.C != null && entities.C.CId.equals(CId)) // 找到了
				result.append("[").append(id1).append(",").append(CId).append(",").append(entities.Id).append(",").append(id2).append("],");
		}
		
		//System.out.println("3-Hop-Rule3 end and total times ："+(System.nanoTime()-st));	
		return result.toString();
	}
	
	
	/**
	 * 使用规则4判断是否有3跳路径
	 * 规则4.id1->id1.FId->step1：找引用id2且F.Fid为id1.FId的论文
	 * 进来了说明id1有fId且有文章引用id2
	 * @return [id1,F.FId,idx,id2], 或者 ""
	 */
	public String IdToId_3Hop_Rule4(String id1,String id2,List<Field> field,List<Entities> PaperRefId2){
		//System.out.println("3-Hop-Rule4 start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		
		Map<String,Integer> map = new HashMap<String,Integer>((int)(field.size()/0.75));
		
		for(Field everyfield : field)
			map.put(everyfield.FId, 1);
		
		for(Entities entities : PaperRefId2) // 遍历每一个引用id2的实体
		{
			if(entities.F.size() == 0) // 这个论文没有所属领域
				continue;
			
			for(Field everyfield : entities.F) // 遍历每一篇论文的FId
			{
				if(map.get(everyfield.FId) != null) // 找到了
					result.append("[").append(id1).append(",").append(everyfield.FId).append(",").append(entities.Id).append(",").append(id2).append("],");
			}
		}
		
		//System.out.println("3-Hop-Rule4 end and total times ："+(System.nanoTime()-st));	
		return result.toString();
	}
	

	/**
	 * 使用规则5判断是否有3跳路径,待提高效率
	 * 其实找作者写的论文的时候就把所有的论文的RId都找出来了，也就没必要再搜一次了
	 * 考虑从这方面优化
	 * 规则5.id1->id1.AA.AuId->找到是这个作者且引用了id2的论文数
	 * @return [id1,AuId,idx,id2], 或者 ""
	 */
	public String IdToId_3Hop_Rule5(String id1,String id2,List<Author>  AA_Info,List<Entities> PaperRefId2){
		//System.out.println("3-Hop-Rule5 start");
		//long st = System.nanoTime();
		
		StringBuilder result = new StringBuilder();
		
		Map<String,Integer> map = new HashMap<String,Integer>((int)(AA_Info.size()/0.75));
		
		for(Author author : AA_Info) // 把每一个作者放入图中
			map.put(author.AuId, 1);
				
		for(Entities entities : PaperRefId2) // 遍历每一个引用id2的实体
		{
			for(Author author : entities.AA) // 遍历每一篇论文的的作者
			{
				if(map.get(author.AuId) != null) // 找到了
					result.append("[").append(id1).append(",").append(author.AuId).append(",").append(entities.Id).append(",").append(id2).append("],");
			}
		}
		
		//System.out.println("3-Hop-Rule5 end and total times ："+(System.nanoTime()-st));	
		return result.toString();
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
	
	/**
	 * 搜索多个id1是否RId为id2用该函数
	 * @param id1
	 * @param id2
	 * @param resultJsonClass
	 * @return [1,2],[2,3],或""
	 */
	public String searchIdArrayToRId(String id1,String id2,ResultJsonClass resultJsonClass){
		String result ="";
		
		int searchResultEntitiesSize = resultJsonClass.entities.size();
		int flag = 0;
        Entities entitiesTemp;
        String searchTemp="";
    	for(int i = 0; i < searchResultEntitiesSize ; ++i)
    	{
//    		//System.out.println(i);
    		entitiesTemp = resultJsonClass.entities.get(i);
    		if(entitiesTemp.Id.equals(id1) || entitiesTemp.Id.equals(id2)) // 是id1或id2 排除，之前已经找过
    			continue;
    		
    		if(flag == 0)
    		{
    			searchTemp = "Id="+entitiesTemp.Id;
    			flag++;
    		}
    		else // 有两个以上entitiesTemp.Id,加Or
    		{
    			searchTemp = addOr(searchTemp, "Id="+entitiesTemp.Id);
    			flag++;
    		}
    		if(flag == 70) // 足够长了，搜索一次
    		{
    	    	// 加And
    	    	searchTemp = addAnd(searchTemp, "RId="+id2);
    	    	// 设置expr
    	    	apiuse.setExpr(searchTemp);
    	    	// 发送请求
    	    	ResultJsonClass searchResult = apiuse.HandleURI(apiuse.getURI());
    	    	// 处理结果
    	    	int searchResultSize = searchResult.entities.size();
    	    	for(int j = 0; j < searchResultSize; ++j)
    	    	{
    	    		result += "["+searchResult.entities.get(j).Id+","+id2+"],";
    	    	}
    	    	flag = 0;
    	    	searchTemp = "";
    		}
    	}

    	if(flag != 0)	// 搜索最后一次
    	{
	    	// 加And
	    	searchTemp = addAnd(searchTemp, "RId="+id2);
	    	// 设置expr
	    	apiuse.setExpr(searchTemp);
	    	// 发送请求
	    	ResultJsonClass searchResult = apiuse.HandleURI(apiuse.getURI());
	    	// 处理结果
	    	int searchResultSize = searchResult.entities.size();
	    	for(int j = 0; j < searchResultSize; ++j)
	    	{
	    		result += "["+searchResult.entities.get(j).Id+","+id2+"],";
	    	}
    	}
		return result;
	}
	
	
}
