package msServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import yulei.mag.api.APIuse;
import yulei.mag.api.ResultJsonClass;
import yulei.mag.api.APIuse.IDtype;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.SolutionAuIdToAuId;
import yulei.mag.api.SolutionAuIdToId;
import yulei.mag.api.SolutionIdToId;
import yulei.mag.api.SolutionIdToAuId;

/*
 * a simple static http server
 */
public class Server {

	private static SolutionIdToId solutionIdToId;
	private static SolutionAuIdToAuId solutionAuIdToAuId;
	private static SolutionAuIdToId solutionAuIdToId;
	private static SolutionIdToAuId solutionIdToAuId;
	private static APIuse apiuse;
	
	public static void main(String[] args) throws Exception {
		solutionIdToId = new SolutionIdToId();
		solutionAuIdToAuId = new SolutionAuIdToAuId();
		solutionAuIdToId = new SolutionAuIdToId();
		solutionIdToAuId = new SolutionIdToAuId();
		
		apiuse = new APIuse();
		HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
		server.createContext("/Bupt", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
		System.out.println("server started");
	}

	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange httpExchange) throws IOException {
			long st = System.nanoTime();
			// 解析url并获取id1和id2
//			long st1 = System.nanoTime();
			String response = null;
			String strUrlParam = new String(httpExchange.getRequestURI().getQuery());
			System.out.println("输入为："+strUrlParam);
			String[] arrSplit = strUrlParam.split("&");
			String[] arrSplitId1 = arrSplit[0].split("=");
			String[] arrSplitId2 = arrSplit[1].split("=");
			String id1 = null;
			String id2 = null;
			if(arrSplitId1[0].equals("id1"))
			{
				id1 = arrSplitId1[1];
				id2 = arrSplitId2[1];
			}
			else
			{
				id1 = arrSplitId2[1];
				id2 = arrSplitId1[1];				
			}
//			System.out.println("字符串处理所用时间： "+(System.nanoTime()-st1));
			// 获取并显示id1和id2的类型
			long st1 = System.nanoTime();
			IDtype id1Type,id2Type;
			
			apiuse.setCount("50000"); // 个数
			apiuse.setAttributes("Id,C.CId,F.FId,J.JId,AA.AuId,AA.AfId,RId"); // 返回的东西，根据路径分析图，id1所有参数都需要
			apiuse.setOffset("0"); // 偏移
			
			String exprId1 = new StringBuilder(27+id1.length()+id1.length())
								.append("Or(Composite(AA.AuId=").append(id1).append("),Id=").append(id1).append(")").toString();
			apiuse.setExpr(exprId1);
	        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
	        List<Entities> EntitiesId1 = searchResultId.entities;
			
			String exprId2 = new StringBuilder(27+id2.length()+id2.length())
			.append("Or(Composite(AA.AuId=").append(id2).append("),Id=").append(id2).append(")").toString();
			apiuse.setExpr(exprId2);
			apiuse.setAttributes("Id,C.CId,F.FId,J.JId,AA.AuId,AA.AfId"); // 返回的东西，根据路径分析图，不需要id2的RId
			searchResultId = apiuse.HandleURI(apiuse.getURI());
	        List<Entities> EntitiesId2 = searchResultId.entities;
	        
	        if(EntitiesId1.size() > 1) // 实体个数大于1肯定是AuId
	        	id1Type = IDtype.AA_AuId;
	        else // 实体个数为1，那就判断实体的Id与id1相等吗
	        {
	        	if(EntitiesId1.get(0).Id.equals(id1)) //相等，id1是论文id
	        		id1Type = IDtype.ID;
	        	else
	        		id1Type = IDtype.AA_AuId;
	        }
	        
	        if(EntitiesId2.size() > 1) // 实体个数大于1肯定是AuId
	        	id2Type = IDtype.AA_AuId;
	        else // 实体个数为1，那就判断实体的Id与id1相等吗
	        {
	        	if(EntitiesId2.get(0).Id.equals(id2)) //相等，id2是论文id
	        		id2Type = IDtype.ID;
	        	else
	        		id2Type = IDtype.AA_AuId;
	        }	        
	        
//	        if(id1Type == IDtype.ID)
//	        	System.out.println("id1="+id1+"类型为Id");
//	        else
//	        	System.out.println("id1="+id1+"类型为AuId");
//	        
//	        if(id2Type == IDtype.ID)
//	        	System.out.println("id2="+id2+"类型为Id");
//	        else
//	        	System.out.println("id2="+id2+"类型为AuId");
	        
			System.out.println("类型判断使用时间： "+(System.nanoTime()-st1));
			// 处理算法

			if(id1Type == id2Type)
			{
				if(id1Type == IDtype.ID) // 两个都是ID
				{
//					response = "[]";
					
					String result = solutionIdToId.IdToId_All(id1, id2,EntitiesId1.get(0),EntitiesId2.get(0));
					response =  new StringBuilder(3+result.length()).append("[")
					.append(result).append("]").toString();
				}
				else // 两个都是AA.AuId
				{
//					response = "[]";
					String result = solutionAuIdToAuId.AuIdToAuId_All(id1, id2,EntitiesId1,EntitiesId2);
					response =  new StringBuilder(3+result.length()).append("[")
					.append(result).append("]").toString();
				}
			}
			else // 一个是ID，一个是AA.AuId
			{
				if(id1Type == IDtype.ID) // 调用Id->AuId函数
				{
//					response = "[]";
					String result = solutionIdToAuId.IdToAuId_All(id1, id2,EntitiesId1.get(0),EntitiesId2);
					response =  new StringBuilder(3+result.length()).append("[")
							.append(result).append("]").toString();
				}
				else // 调用AuId->Id函数
				{
//					response = "[]";
					String result = solutionAuIdToId.AuIdToId_All(id1, id2,EntitiesId1,EntitiesId2.get(0));
					response =  new StringBuilder(3+result.length()).append("[")
							.append(result).append("]").toString();;
				}
				
			}
			String temp1 = response.replace("[", "");
			System.out.println("路径个数："+ (response.length() - temp1.length() - 1));
			System.out.println("solution total times "+(System.nanoTime()-st));
//			System.out.println(response);
			// 返回给请求方
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		
	}

}


