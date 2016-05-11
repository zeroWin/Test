package msServer;

import java.net.URI;

import org.apache.http.client.utils.URIBuilder;

//// This sample uses the Apache HTTP client from HTTP Components (http://hc.apache.org/httpcomponents-client-ga/)


import com.google.gson.GsonBuilder;

import yulei.mag.api.APIuse;
import yulei.mag.api.ResultJsonClass;

public class MAGAPIuse
{
 public static void main(String[] args) 
 {
     GsonBuilder gsonBuilder = new GsonBuilder();
     APIuse apiUse = new APIuse();
     gsonBuilder.serializeNulls();
     gsonBuilder.setPrettyPrinting().serializeNulls();
     try
     {
//         URIBuilder builder = new URIBuilder("https://oxfordhk.azure-api.net/academic/v1.0/evaluate");
//         
//
//         builder.setParameter("expr", "RId=2140251882");
//         // 同时搜多个的方法
//         //builder.setParameter("expr", "Composite(AA.AuId=2251253715)");
//         //builder.setParameter("expr", "And(Composite(AA.AuId=2294766364),Id=2140251882)");
//         //builder.setParameter("expr", "Or(RId=2143554828,Id=2140251882)");
//         builder.setParameter("count", "10000");
//         //builder.setParameter("attributes", "Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
//         builder.setParameter("attributes", "Id");
//         builder.setParameter("subscription-key", "f7cc29509a8443c5b3a5e56b0e38b5a6");
//         //builder.setParameter("orderby","Id:asc");
//         URI uri = builder.build();
//         ResultJsonClass searchResult1 = apiUse.HandleURI(uri);
//         apiUse.showResultAsJsonFormat(searchResult1);
         
         
    	 // 这三个参数必须设置
    	 apiUse.setExpr("Id=2140251882");
    	 apiUse.setCount("10000");
    	 apiUse.setAttributes("RId");
    	 //获取搜索结果
         ResultJsonClass searchResult = apiUse.HandleURI(apiUse.getURI());
         // 显示结果
         //apiUse.showResultAsJsonFormat(searchResult);

         // 再次设定参数，会将前一次的覆盖
         apiUse.setExpr("RId=2036218035");
    	 //获取搜索结果
         searchResult = apiUse.HandleURI(apiUse.getURI());
         // 显示结果
         //apiUse.showResultAsJsonFormat(searchResult);         
         
         // 再次设定参数
         apiUse.setExpr("Composite(AA.AuId=2251253715)");
         apiUse.setOffset("1");
         apiUse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
         //获取搜索结果
         searchResult = apiUse.HandleURI(apiUse.getURI());
         // 显示结果
         apiUse.showResultAsJsonFormat(searchResult);          
         
         // 直接通过searchResult获取结果的方法
         System.out.println(searchResult.expr);
         System.out.println(searchResult.entities.get(0).logprob);
         System.out.println(searchResult.entities.get(0).Id);
         System.out.println(searchResult.entities.get(0).RId.get(1));
         System.out.println(searchResult.entities.get(0).AA.get(0).AfId);
         System.out.println(searchResult.entities.get(0).C.CId);
         System.out.println(searchResult.entities.get(0).J.JId);
 
         // 也可通过函数获取各个参数
         // 
     }
     catch (Exception e)
     {
         System.out.println(e.getMessage());
     }
 }
}