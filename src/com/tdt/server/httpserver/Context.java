package com.tdt.server.httpserver;

import java.util.HashMap;
import java.util.Map;


import com.tdt.server.httpserver.core.impl.HttpHandler;

/**
 * 
 * @author chuer
 * @Description: 上下文 
 * @date 2014年11月12日 下午3:53:48 
 * @version V1.0
 */
public class Context {
	private static Map<String,HttpHandler> contextMap = new HashMap<String,HttpHandler>();
	public static String contextPath = "Bupt";
	public static void load(){
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static HttpHandler getHandler(String key){
		return contextMap.get(key);
	}
	
}
