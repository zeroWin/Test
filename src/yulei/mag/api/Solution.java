package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.APIuse;
import yulei.mag.api.ResultJsonClass;
import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.ResultJsonClass.Field;
import yulei.mag.api.ReturnResult;

public class Solution {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	
	public Solution(){
		apiuse = new APIuse();
	};
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//	    apiuse = new APIuse();
//	    returnResult = new ReturnResult();
//	    long a = System.nanoTime();
//		System.out.println(IdToId_1Hop("2143157063","2025603036"));
//		long b = System.nanoTime();
//		System.out.println(b-a);
//		
//		
//
//	    a = System.nanoTime();
//		System.out.println(IdToId_2Hop("2143157063","2025603036"));
//		b = System.nanoTime();
//		System.out.println(b-a);
//	}

	public String IdToId_All(String id1,String id2)
	{
		String result = "";

		apiuse.setCount("10000");
		apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		apiuse.setOffset("0");
		// 1-hop
//		String path1Hop = IdToId_1Hop(id1, id2);
//		if(path1Hop.length() != 0)
//			result += "["+path1Hop+"]" + ",";
		
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
//		String path2Hop = IdToId_2Hop(id1, id2,EntitiesId1,EntitiesId2);
//		if(path2Hop.length() != 0)
//			result += path2Hop + ",";
		
		// 3-hop
		String path3Hop = IdToId_3Hop(id1,id2,EntitiesId1,EntitiesId2);
		if(path3Hop.length() != 0)
			result += path3Hop + ",";
		
		int t = result.length();
		if(t != 0) // 去掉最后的逗号
			return result.substring(0, result.length()-1);
		return result;
	}
	
	/**
	 * 用来判断1-hop，id1.Rid = id2，找到所有id到id的一跳路径
	 * @param id1
	 * @param id2
	 * @return 1,2 或者 ""
	 */
	public String IdToId_1Hop(String id1,String id2){
		String expr = "And("+"Id="+id1+","+"RId="+id2+")";
		apiuse.setExpr(expr);
		//apiuse.setCount("10000");
		//apiuse.setAttributes("AA.AuId");
		//apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		
		//获取搜索结果	
        ResultJsonClass searchResult = apiuse.HandleURI(apiuse.getURI());
        // 实体个数为0，或者搜到实体作者为0
        if(apiuse.GetEntitiesNumFromJson(searchResult) == 0 
        		|| apiuse.GetAANumFromJson(searchResult.entities.get(0)) == -1)
        {
//        	System.out.println("Id1->Id2不存在");
        	return "";
        }
        
        // 路径存在
        return id1+","+id2;
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
	public String IdToId_2Hop(String id1,String id2,Entities EntitiesId1,Entities EntitiesId2){
		System.out.println("2-Hop start");
		String result = "";
		long st = System.nanoTime();
		
		// 判断JID 期刊 规则6，7
		System.out.println("ID1->JId->ID2:");
		if(EntitiesId1.J != null && EntitiesId2.J != null && EntitiesId1.J.JId == EntitiesId2.J.JId)
		{
			String temp = "["+id1+","+EntitiesId1.J.JId+","+id2+"],";
			result += temp;
			System.out.println(temp);
		}	
		
		// 判断CID 会议 规则4，5
		System.out.println("ID1->CId->ID2:");
		if(EntitiesId1.C != null && EntitiesId2.C != null && EntitiesId1.C.CId == EntitiesId2.C.CId)
		{
			String temp = "["+id1+","+EntitiesId1.C.CId+","+id2+"],";
			result += temp;
			System.out.println(temp);
		}
		
		// 判断FId 领域 规则2，3
		System.out.println("ID1->FId->ID2:");
		if(EntitiesId1.F != null  && EntitiesId2.F != null 
		   && EntitiesId1.F.size() != 0 && EntitiesId2.F.size() != 0)
		{
			// 两个链表找出相同元素
			String temp = IdToId_2Hop_FId(id1,id2,EntitiesId1.F,EntitiesId2.F);
			result += temp;
			System.out.println(temp);
		}
        
	
		// 判断 AA.AuId 作者 规则10，11
		System.out.println("ID1->AA.AuId->ID2:");
		if(EntitiesId1.AA != null && EntitiesId2.AA != null 
			&& EntitiesId1.AA.size() != 0 && EntitiesId2.AA.size() != 0)
		{
			String temp = IdToId_2Hop_AA_AuId(id1,id2,EntitiesId1.AA,EntitiesId2.AA);
			result += temp;
			System.out.println(temp);
		}
		
		// 判断RID 参考文献 规则1 会将uri的AttributesAA.AuId;
		System.out.println("ID1->RId->RId = ID2:");
		if(EntitiesId1.RId != null && EntitiesId1.RId.size() != 0)
		{
			String temp = IdToId_2Hop_RId(id1,id2,EntitiesId1.RId);
			result += temp;
			System.out.println(temp);
		}
		
		System.out.println("2-Hop end and total times :"+(System.nanoTime()-st));
		// 返回结果
		int t = result.length();
		if(t != 0) // 去掉最后的逗号
			return result.substring(0, result.length()-1);
		return result;
		
	}
	

	/**
	 * 找到链表1和链表2相同的FID
	 * @param list1
	 * @param list2
	 * @return
	 */
	private String IdToId_2Hop_FId(String id1,String id2,List<Field> list1,List<Field> list2){
		System.out.println("2-Hop-FId start");
		String result = "";
		long st = System.nanoTime();
		List<Field> maxList = list1;
		List<Field> minList = list2;
		if(list2.size() > list1.size())
		{
			maxList = list2;
			minList = list1;
		}
		Map<String,Integer> map = new HashMap<String,Integer>(maxList.size());  
		for(Field field : maxList)
		{
			map.put(field.FId, 1);
		}
		
		for(Field field : minList){
			if(map.get(field.FId) != null){ // 
				result += "["+id1+","+field.FId+","+id2+"],";
			}
		}
		
		System.out.println("2-Hop-Fid end and total times :"+(System.nanoTime()-st));
		return result;
	}
	
	/**
	 * 找到链表1的RId的Rid是id2，及id1的参考文献的参考文献中有id2
	 * @param id1 id1的编号
	 * @param id2 id2的编号
	 * @param list1 id1的所有Rid
	 * @return [1,2],[3,4],或""
	 */
	private String IdToId_2Hop_RId(String id1,String id2,List<String> list1){
		System.out.println("2-Hop-RId start");
		String result = "";
		long st = System.nanoTime();
		int listLen = list1.size();
		String searchTemp = "";
		int flag = 0;
		for(int i = 0; i < listLen; ++i)
		{
			System.out.println(i);
			if(flag == 0)
    		{
    			searchTemp = "Id="+list1.get(i);
    			flag++;
    		}
			else // 有两个以上Id,加Or
    		{
    			searchTemp = addOr(searchTemp, "Id="+list1.get(i));
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
		System.out.println("2-Hop-RId end and total times ："+(System.nanoTime()-st));
		
		return result;
	}
	
	/**
	 * 找到链表1和链表2相同的AA.AuId
	 * @param list1
	 * @param list2
	 * @return
	 */
	private String IdToId_2Hop_AA_AuId(String id1,String id2,List<Author> list1,List<Author> list2){
		System.out.println("2-Hop-AA.AuId start");
		String result = "";
		long st = System.nanoTime();
		List<Author> maxList = list1;
		List<Author> minList = list2;
		if(list2.size() > list1.size())
		{
			maxList = list2;
			minList = list1;
		}
		Map<String,Integer> map = new HashMap<String,Integer>(maxList.size());  
		for(Author author : maxList)
		{
			map.put(author.AuId, 1);
		}
		
		for(Author author : minList){
			if(map.get(author.AuId) != null){ // 找到了
				result += "["+id1+","+author.AuId+","+id2+"],";
			}
		}
		
		System.out.println("2-Hop-AA.AuId end and total times ："+(System.nanoTime()-st));
		return result;
	}
	
	/* 下面代码为3-hop 判断相关代码 */
	/**
	 * 用来判断所有规则 找到所有id1到id2的3跳路径
	 * 1.id1->id1.RId 找所有id1.RId到id2的2跳路径
	 * 2.id1->id1.JId->step1：找出该期刊所有论文->step2：判断这些论文是否引用id2
	 * @param id1
	 * @param id2
	 * @return
	 */
	public String IdToId_3Hop(String id1,String id2,Entities EntitiesId1,Entities EntitiesId2){
		String result = "";
		
		// 1.id1->id1.RId 找所有id1.RId到id2的2跳路径
		if(EntitiesId1.RId != null && EntitiesId1.RId.size() != 0)
		{
//			result += IdToId_3Hop_Rule1(id1,id2,EntitiesId1.RId,EntitiesId2);
		}
		
		// 2.id1->id1.JId->step1：找出该期刊所有论文->step2：判断这些论文是否引用id2
		if(EntitiesId1.J != null)
		{
			result += IdToId_3Hop_Rule2(id1,id2,EntitiesId1.J.JId);
		}
		
		// 返回结果
		int t = result.length();
		if(t != 0) // 去掉最后的逗号
			return result.substring(0, result.length()-1);
		return result;
	}
	
	/**
	 * 使用规则1判断是否有3跳路径
	 * 规则1：id1->id1.RId 找所有id1.RId到id2的2跳路径
	 * @return [1,2,3,4],[3,4,5,6], 或者 ""
	 */
	public String IdToId_3Hop_Rule1(String id1,String id2,List<String> ID1_RIdlist,Entities EntitiesId2){
		System.out.println("3-Hop-Rule1 start");
		String result = "";
		long st = System.nanoTime();

		// 找每个string到id2的2跳路径
		for(String string : ID1_RIdlist)
		{
			System.out.println("tttt:"+string);
	        // Get string search result
			String expr = "Id="+string;
			apiuse.setExpr(expr);
			ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
			//apiuse.showResultAsJsonFormat(searchResultId);
			Entities EntitiesIdString = searchResultId.entities.get(0);
			
			// 返回的是[],[],[]这样的字符串
			String temp = IdToId_2Hop(string,id2,EntitiesIdString,EntitiesId2);
//			System.out.println("id1.Rid->id2 2跳路径："+temp);
			if(temp.length() != 0) // 
			{
				// 把所有[用[id1,替换
//				System.out.println("替换前："+temp);
				temp  = temp.replace("[","["+id1+",");
//				System.out.println("替换后:"+temp);
				result += temp + ",";
			}
				
		}
		System.out.println("3-Hop-Rule1 end and total times ："+(System.nanoTime()-st));		
		
		
		
		return result;
	}
	
	/**
	 * 使用规则2判断是否有3跳路径
	 * 规则2：id1->id1.JId->step1：找出该期刊所有论文->step2：判断这些论文是否引用id2
	 * @return [1,2,3,4],[3,4,5,6], 或者 ""
	 */
	public String IdToId_3Hop_Rule2(String id1,String id2,String JId){
		System.out.println("3-Hop-Rule2 start");
		long st = System.nanoTime();
		String result = "";

		String expr = "Composite(J.JId="+JId+")";
		apiuse.setExpr(expr);
		//获取搜索结果
        ResultJsonClass searchResult = apiuse.HandleURI(apiuse.getURI());		
		
        int searchResultEntitiesSize = searchResult.entities.size();
        while(searchResultEntitiesSize == 10000)
        {
        	result += searchIdArrayToRId(id1,id2,searchResult);
        	searchResult = apiuse.HandleURI(apiuse.getURI());
        	searchResultEntitiesSize = searchResult.entities.size();
        }
        
        System.out.println("JId = "+JId+"论文数为："+searchResultEntitiesSize);
        result += searchIdArrayToRId(id1,id2,searchResult);
    	// 把所有[替换成[id1,JId,
		result = result.replace("[", "["+id1+","+JId+",");
		apiuse.setOffset("0");
		System.out.println("3-Hop-Rule2 end and total times ："+(System.nanoTime()-st));	
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
    		System.out.println(i);
    		entitiesTemp = resultJsonClass.entities.get(i);
    		if(entitiesTemp.Id == id1 || entitiesTemp.Id == id2) // 是id1或id2 排除，之前已经找过
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
