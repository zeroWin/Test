package msServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class testMAIN {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		  long st = System.nanoTime(); 
//		  String result = "";
//	      List<String> list1 = new ArrayList<String>();  
//	      List<String> list2 = new ArrayList<String>();  
//	         
//	      for (int i = 0; i < 10000; i++) {  
//	            list1.add("test"+i);  
//	            list2.add("test"+i*2);  
//	      } 
//	         List<String> diff = new ArrayList<String>();  
//	         List<String> maxList = list1;  
//	         List<String> minList = list2;  
//
//	         if(list2.size()>list1.size())  
//	         {  
//	             maxList = list2;  
//	             minList = list1;  
//	         }  
////	         Map<String,Integer> map = new HashMap<String,Integer>(maxList.size());
////	         System.out.println(map.isEmpty());
////	         for (String string : maxList) {  
////	             map.put(string, 1);  
////	         }  
////	         System.out.println(map.isEmpty());
////	         for (String string : maxList) {  
////	             map.remove(string, 1);  
////	         }  
////	         System.out.println(map.isEmpty());
//	         Map<String,String> map1 = new HashMap<String,String>((int)(maxList.size()/0.75));
//	         System.out.println(map1.size());
//	         for (String string : maxList) {  
//	             map1.put(string, string+"haha"); 
//	             map1.get(string);
//	         }  
//	         System.out.println(map1.isEmpty());
//	         for (String string : minList) {  
//	             if(map1.get(string)!=null)  
//	             {  
//	            	 //System.out.println(map1.get(string));
//	            	 //result = result +","+string;
//	             }  
//	         }  
//	         System.out.println("getDiffrent5 total times "+(System.nanoTime()-st));  
		long st = System.nanoTime(); 
		for(int i = 0;i<1000000;++i)
			concat1("123456789","","123456789","","123456789","123456789");
		System.out.println("方式1时间： "+(System.nanoTime()-st));
		
		
		st = System.nanoTime(); 
		for(int i = 0;i<1000000;++i)
			concat2("123456789","123456789","","123456789","","123456789");
		System.out.println("方式2时间： "+(System.nanoTime()-st));
		
		
		st = System.nanoTime(); 
		for(int i = 0;i<1000000;++i)
			concat3("123456789","123456789","","123456789","","123456789");
		System.out.println("方式3时间： "+(System.nanoTime()-st));
		
		
		st = System.nanoTime(); 
		for(int i = 0;i<1000000;++i)
			concat4("123456789","123456789","","123456789","","123456789");
		System.out.println("方式4时间： "+(System.nanoTime()-st));
		
		st = System.nanoTime(); 
		for(int i = 0;i<1000000;++i)
			concat5("12345678900","123456789","","","","");
		System.out.println("方式5时间： "+(System.nanoTime()-st));
//		concat5("123", "", "", "213" ,"df", "");
	
		st = System.nanoTime(); 
		StringBuilder str = new StringBuilder();
		for(int i = 0;i<1000000;++i)
			str.append("213123123").append("123").append("21321312").append("2131");
		System.out.println("方式xxx时间： "+(System.nanoTime()-st));
	//	String result = "";
		String test = null;
		System.out.println(test);
		st = System.nanoTime(); 
		for(int i = 0;i<1000000;++i)
		{
			String result = null;

			result = concat5("123456","123123124","","","","");
		}
		System.out.println("方式7时间： "+(System.nanoTime()-st));
		
		String str1 = "2132131232134";
		String str2 = "123213213123";
		st = System.nanoTime();
		for(int i = 0;i<1000000;++i)
		{
			str1.equals(str2);
		}
		System.out.println("字符串相等时间： "+(System.nanoTime()-st));
		
		
		StringBuilder stringTemp = new StringBuilder();
		if(stringTemp.toString().equals(""))
		System.out.println(123);
		
		st = System.nanoTime(); 
			System.out.println("显示一下是多长时间呢");
		System.out.println("方式3时间： "+(System.nanoTime()-st));
	}
	
	public static String concat1(String s1, String s2, String s3, String s4, String s5, String s6) {
        String result = "";
        result += s1;
        result += s2;
        result += s3;
        result += s4;
        result += s5;
        result += s6;
        return result;
    }

    public static String concat2(String s1, String s2, String s3, String s4, String s5, String s6) {
        StringBuffer result = new StringBuffer();
        result.append(s1);
        result.append(s2);
        result.append(s3);
        result.append(s4);
        result.append(s5);
        result.append(s6);
        return result.toString();
    }

    public static String concat3(String s1, String s2, String s3, String s4, String s5, String s6) {
        return new StringBuffer(s1.length() + s2.length() + s3.length() + s4.length() + s5.length() + s6.length())
                .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString();
    }

    public static String concat4(String s1, String s2, String s3, String s4, String s5, String s6) {
        return s1 + s2 + s3 + s4 + s5 + s6;
    }

    public static String concat5(String s1, String s2, String s3, String s4, String s5, String s6) {
//        System.out.println(new StringBuilder(s1.length() + s2.length() + s3.length() + s4.length() + s5.length() + s6.length())
//                .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString());
        return new StringBuilder(s1.length() + s2.length() + s3.length() + s4.length() + s5.length() + s6.length())
                .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString();

    }
    
    public static String concat6(String s1, String s2, String s3, String s4, String s5, String s6) {
//      System.out.println(new StringBuilder(s1.length() + s2.length() + s3.length() + s4.length() + s5.length() + s6.length())
//              .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString());
      return new StringBuilder()
              .append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).toString();

  }
 

}
