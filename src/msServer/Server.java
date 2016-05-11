package msServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;



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
			// 解析url并获取id1和id2
			String response = "";
			String strUrlParam = new String(httpExchange.getRequestURI().getQuery());
			String[] arrSplit = strUrlParam.split("&");
			String id1 = arrSplit[0].split("=")[1];
			String id2 = arrSplit[1].split("=")[1];
			
			// 获取并显示id1和id2的类型			
			APIuse apiuse = new APIuse();
			IDtype id1Type,id2Type;
			id1Type = apiuse.GetIdType(id1);
			id2Type = apiuse.GetIdType(id2);
			
			
			// 处理算法
			long st = System.nanoTime();
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
			System.out.println("solution total times "+(System.nanoTime()-st));
			System.out.println(response);
			// 返回给请求方
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		
	}

}


