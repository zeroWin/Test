package msServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import yulei.mag.api.APIuse;
import yulei.mag.api.ResultJsonClass;

public class HandleFile {

	public static void main(String[] args) throws IOException {
		// 把微软的数据库扒下来
		// TODO Auto-generated method stub
		File diskPartition = new File("D:");
	    APIuse apiUse = new APIuse();
	    long IdNum =    572550L;
	    long endNum =   600000L;
	    //long endNum = 1000000L;
	    long WriteNum = 0L;
	    apiUse.setCount("50000"); // 个数
	    apiUse.setAttributes("Id,C.CId,F.FId,J.JId,AA.AuId,AA.AfId,RId"); // 返回的东西，根据路径分析图，id1所有参数都需要
	    apiUse.setOffset("0"); // 偏移
	    
		HttpClient httpclient = HttpClients.createDefault();;
		HttpResponse response;
		Gson gson = new Gson();
		long FreeSpace;
		StringBuilder expr = new StringBuilder();
		ResultJsonClass searchResult;
		File fileWrite;
		FileOutputStream fos;
		long numTemp;
		HttpEntity entity;
		String result;
	    for(;IdNum < endNum;++IdNum)
	    {
	    	FreeSpace = (diskPartition.getFreeSpace()/1024/1024);
	    	if(FreeSpace < 50) // 小于50MB
	    	{
	    		System.out.println("没空间了");
	    		continue;	// 死循环
	    	}
	    	expr.append("Id=").append(Long.toString(IdNum));
	    	apiUse.setExpr(expr.toString());
	    	// 搜索
	    	response = httpclient.execute(new HttpGet(apiUse.getURI()));
	    	entity = response.getEntity();
	    	
	    	if(entity != null)
	    	{
	        	 result = EntityUtils.toString(entity);
	        	 //System.out.println(result);
	        	 searchResult = gson.fromJson(result, ResultJsonClass.class);
	        	 if(searchResult.entities != null && searchResult.entities.size() != 0 
	        			 && searchResult.entities.get(0).AA != null
	        			 && searchResult.entities.get(0).AA.size() != 0) // 是Id
	        	 {
	        		 
	        		 System.out.println("写入"+expr+"的文件，磁盘剩余空间="+FreeSpace);
	        		 // 创建文件
	        		 fileWrite = new File("D:\\Id\\"+expr.toString()+".txt");
	        		 fileWrite.createNewFile();
	        		 // 写入文件
	        		 fos =  new FileOutputStream(fileWrite); 
	        		 fos.write(result.getBytes());
	        		 fos.close();
	        		 WriteNum++;
	        	 }
	    	}
	    	expr = new StringBuilder();
	    	numTemp = IdNum + 1;
	    	if(numTemp % 50 == 0)
	    	System.out.println("当前搜索为第"+numTemp+"次。写入次数为"+WriteNum);    	
	    }
	}

}
