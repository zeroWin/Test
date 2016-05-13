package msServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import yulei.mag.api.APIuse;
import yulei.mag.api.APIuse.IDtype;
import yulei.mag.api.Solution;

/*
 * a simple static http server
 */
public class Server {

	private static Solution solution;
	
	public static void main(String[] args) throws Exception {
		solution = new Solution();
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
			String response = "";
			String strUrlParam = new String(httpExchange.getRequestURI().getQuery());
			System.out.println("输入为："+strUrlParam);
			String[] arrSplit = strUrlParam.split("&");
			String[] arrSplitId1 = arrSplit[0].split("=");
			String[] arrSplitId2 = arrSplit[1].split("=");
			String id1 = "";
			String id2 = "";
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
			
//			for(String s : httpExchange.getRequestHeaders().keySet()){
//				System.out.print(httpExchange.getRequestHeaders().get(s));
//			}
			// 获取并显示id1和id2的类型			
			APIuse apiuse = new APIuse();
			IDtype id1Type,id2Type;
			id1Type = apiuse.GetIdType(id1);
			id2Type = apiuse.GetIdType(id2);
			
			// 处理算法

			if(id1Type == id2Type)
			{
				if(id1Type == IDtype.ID) // 两个都是ID
				{
					response = solution.IdToId_All(id1, id2);
					
					response = "["+response+"]";
				}
				else // 两个都是AA.AuId
				{
					response = "[["+id1+","+id2+"]]";
				}
			}
			else // 一个是ID，一个是AA.AuId
			{
				response = "[["+id1+","+id2+"]]";
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


