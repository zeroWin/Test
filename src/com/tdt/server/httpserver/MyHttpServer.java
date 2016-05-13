package com.tdt.server.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
/**
 * 
 * @author chuer
 * @Description: 服务器启动类
 * @date 2014年11月12日 下午3:53:38 
 * @version V1.0
 */
public class MyHttpServer {
    //启动服务，监听来自客户端的请求
	public static void start() throws IOException {
		Context.load();
		
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver =provider.createHttpServer(new InetSocketAddress(80), 100);//监听端口8080,能同时接 受100个请求
		httpserver.createContext("/Bupt", new MyHttpHandler()); 
		httpserver.setExecutor(null);
		httpserver.start();
		System.out.println("server started");
	}
	
	
	public static void main(String[] args) throws IOException {
		start();
	}
}
