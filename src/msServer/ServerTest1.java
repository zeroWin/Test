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
public class ServerTest1 {

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
			
	        //HttpRequest request = new HttpRequest(httpExchange);  
	        //HttpResponse response = new HttpResponse(httpExchange);  

			// 返回给请求方

		}
		
	}

}


