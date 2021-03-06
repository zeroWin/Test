package yulei.mag.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.ResultJsonClass.Field;

public final class APIuse {
	public enum IDtype{
		ID,AA_AuId;
	}
	private URIBuilder builder;
	private HttpClient httpclient;
	private HttpResponse response;
	private URIBuilder builderForType;
	
	public APIuse(){
	     try
	     {
	         builder = new URIBuilder("https://oxfordhk.azure-api.net/academic/v1.0/evaluate");
	         builder.setParameter("subscription-key", "f7cc29509a8443c5b3a5e56b0e38b5a6");
	         httpclient = HttpClients.createDefault();
	         
	         
	         builderForType = new URIBuilder("https://oxfordhk.azure-api.net/academic/v1.0/evaluate");
	         builderForType.setParameter("subscription-key", "f7cc29509a8443c5b3a5e56b0e38b5a6");
		     builderForType.setParameter("count", "10000");
		     builderForType.setParameter("attributes", "AA.AuId,AA.AfId");
		     
		     HandleURI(new URI("https://oxfordhk.azure-api.net/academic/v1.0/evaluate?expr=Id=2140251882&count=10000&attributes=Id&subscription-key=f7cc29509a8443c5b3a5e56b0e38b5a6"));
	     }
	     catch (Exception e)
	     {
	         System.out.println(e.getMessage());
	     }
	}
	
	/**
	 * 设置Uri的Expr变量
	 * 该变量表示你的搜索条件
	 * @author zero
	 * @param expr
	 */
	public void setExpr(String expr){
		builder.setParameter("expr", expr);
	}
	
	/**
	 * 设置Uri的count变量
	 * 该变量表示你要求返回结果的最大个数
	 * @author zero
	 * @param expr
	 */
	public void setCount(String count){
		builder.setParameter("count", count);
	}	
	
	/**
	 * 设置Uri的attributes变量
	 * 该变量表示你要求返回什么数据
	 * @author zero
	 * @param uri
	 */
	public void setAttributes(String attributes){
		builder.setParameter("attributes", attributes);
	}		

	/**
	 * 设置Uri的attributes变量
	 * 该变量表示你要求返回什么数据
	 * @author zero
	 * @param uri
	 */
	public void setOffset(String offset){
		builder.setParameter("offset", offset);
	}	
	/**
	 * 获取前面配置好生成的uri地址
	 * @return uri 生成uri地址
	 */
	public URI getURI(){
		URI uri = null;
		try {
			uri = builder.build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}
	
	
	public ResultJsonClass HandleURI(URI uri){
		ResultJsonClass searchResult = null;
		Gson gson = new Gson();
		try
		{
			// 发送http请求
//			long st1 = System.nanoTime();
//			HttpGet request = new HttpGet(uri);
////
//			this.response = this.httpclient.execute(request);
//			
//	        HttpEntity entity = this.response.getEntity();
//	        
//////	        System.out.println("搜索 total times :"+(System.nanoTime()-st1));
//////	        st1 = System.nanoTime();
//	        if (entity != null) 
//	        {
//	        	 String result = EntityUtils.toString(entity);
//	        	 System.out.println(result);
//	        	 searchResult = gson.fromJson(result, ResultJsonClass.class);
//	        }
			
			response = httpclient.execute(new HttpGet(uri));
			searchResult = gson.fromJson(EntityUtils.toString(response.getEntity()), ResultJsonClass.class);
//	        System.out.println("字符串转换 total times :"+(System.nanoTime()-st1));
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return searchResult;
	}
	
	public void showResultAsJsonFormat(ResultJsonClass resultJsonClass){
	     GsonBuilder gsonBuilder = new GsonBuilder();
	     gsonBuilder.serializeNulls();
	     gsonBuilder.setPrettyPrinting().serializeNulls();
	     Gson gson = gsonBuilder.create();
	     System.out.println(gson.toJson(resultJsonClass));
	}
	
	public int GetEntitiesNumFromJson(ResultJsonClass resultJsonClass){
		return resultJsonClass.entities.size();
	}
	
	// 获取ID
	public String GetIdFromJson(Entities entities){
		return entities.Id;
	}
	
	// 获取C.CId 会议
	// 个人理解（不一定对）：
	// 1.该值只会有1个或者null。不能投多个会议。
	// 2.与J.JId冲突，即投会议的不会投到期刊上
	public String GetCIdFromJson(Entities entities){
		if(entities.C != null)
			return entities.C.CId;
		return null;
	}
	
	// 获取J.JId 期刊
	// 个人理解（不一定对）：
	// 1.该值只会有1个或者null。不能投多个期刊。
	// 2.与C.CId冲突，即投期刊的不会投到会议上
	public String GetJIdFromJson(Entities entities){
		if(entities.J != null)
			return entities.J.JId;
		return null;
	}
	
	// 获取RId
	public List<String> GetRIdFromJson(Entities entities){
		if(entities.RId != null)
			return entities.RId;
		return null;
	}
	
	// 获取RId的个数
	public int GetRIdNumFromJson(Entities entities){
		if(entities.RId != null)
			return entities.RId.size();
		return -1;
	}
	
	// 获取AA
	public List<Author> GetAAFromJson(Entities entities){
		if(entities.AA != null)
			return entities.AA;
		return null;
	}
	
	// 获取AA(作者)的个数
	public int GetAANumFromJson(Entities entities){
		if(entities.AA != null)
			return entities.AA.size();
		return -1;
	}	
	
	// 获取F.FId 所属领域
	// 个人理解：
	// 1.该值可能是null或者>=1，因为论文可能包含在多个领域
	public List<Field> GetFIdFromJson(Entities entities){
		if(entities.F != null)
			return entities.F;
		return null;
	}	
	
	// 获取F.FId的个数
	/**
	 * @param entities
	 * @return
	 */
	public int GetFIdNumFromJson(Entities entities){
		if(entities.F != null)
			return entities.F.size();
		return -1;
		
	}		
	
	// 获取输入id的类型是Id还是AA.AuId
	public IDtype GetIdType(String Id){
		IDtype idType = IDtype.ID;
		try
		{

			//String uri = "https://oxfordhk.azure-api.net/academic/v1.0/evaluate?count=10000&attributes=AA.AuId,AA.AfId&subscription-key=f7cc29509a8443c5b3a5e56b0e38b5a6";
			//uri = uri + "&expr=Id="+Id;
			this.builderForType.setParameter("expr","Id="+Id);
//			long st = System.nanoTime();
			ResultJsonClass resultJsonClass = HandleURI(this.builderForType.build());
//			System.out.println("Search times "+(System.nanoTime()-st));
	        // 搜索ID得不到结果或者搜索到的ID里面的作者是空的
	        if(resultJsonClass.entities == null || resultJsonClass.entities.size() == 0 || resultJsonClass.entities.get(0).AA == null
	        		|| resultJsonClass.entities.get(0).AA.size() == 0)
	        {
	        	idType = IDtype.AA_AuId;
	        	System.out.println(Id+"类型为：AA_AuId");
	        }
	        else
	        {
	        	idType = IDtype.ID;
	        	System.out.println(Id+"类型为：ID");
	        }
		}
	    catch (Exception e)
	    {
	    	System.out.println(e.getMessage());
	    }
		return idType;
	}
}
