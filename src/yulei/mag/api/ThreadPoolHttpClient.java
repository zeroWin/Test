package yulei.mag.api;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class ThreadPoolHttpClient {
    // 线程池
    private ExecutorService exe = null;
    // 线程池的容量
    private static final int POOL_SIZE = 20;
    private HttpClient client = null;
    String[] urls=null;
    public ThreadPoolHttpClient(String[] urls){
        this.urls=urls;
    }
    
    
    public void test() throws Exception {
        exe = Executors.newFixedThreadPool(POOL_SIZE);

        final HttpClient httpClient = HttpClients.createDefault();

        // URIs to perform GETs on
        final String[] urisToGet = urls;
        /* 有多少url创建多少线程，url多时机子撑不住 */
        // create a thread for each URI
        GetThread[] threads = new GetThread[urisToGet.length];
        for (int i = 0; i < threads.length; i++) {
            HttpGet httpget = new HttpGet(urisToGet[i]);
            threads[i] = new GetThread(httpClient, httpget,i);            
        }
        // start the threads
        for (int j = 0; j < threads.length; j++) {
            threads[j].start();
        }

        // join the threads，等待所有请求完成
        for (int j = 0; j < threads.length; j++) {
            threads[j].join();
        }
      
        System.out.println("Done");
    }
    static class GetThread extends Thread{
        
        private final HttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int i;
        public GetThread(HttpClient httpClient, HttpGet httpget,int i) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.i = i;
        }
        @Override
        public void run(){
            this.setName("threadsPoolClient");
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            get();
        }
        
        public ResultJsonClass get() {
    		ResultJsonClass searchResult = null;
    		Gson gson = new Gson();
            try {
                HttpResponse response = this.httpClient.execute(this.httpget, this.context);
                HttpEntity entity = response.getEntity();
                if (entity != null) 
                {
	   	        	 String result = EntityUtils.toString(entity);
//	   		         System.out.println(result);
	   	        	 System.out.println(System.nanoTime()+"："+i);
		        	 searchResult = gson.fromJson(result, ResultJsonClass.class);
	        	 }
                // ensure the connection gets released to the manager
                EntityUtils.consume(entity);
            } catch (Exception ex) {
                this.httpget.abort();
            }finally{
                httpget.releaseConnection();
            }
            return searchResult;
        }
    }
}
