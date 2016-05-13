package msServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class testMAIN {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		  long st = System.nanoTime(); 
		  String result = "";
	      List<String> list1 = new ArrayList<String>();  
	      List<String> list2 = new ArrayList<String>();  
	         
	      for (int i = 0; i < 10000; i++) {  
	            list1.add("test"+i);  
	            list2.add("test"+i*2);  
	      } 
	         List<String> diff = new ArrayList<String>();  
	         List<String> maxList = list1;  
	         List<String> minList = list2;  

	         if(list2.size()>list1.size())  
	         {  
	             maxList = list2;  
	             minList = list1;  
	         }  
//	         Map<String,Integer> map = new HashMap<String,Integer>(maxList.size());
//	         System.out.println(map.isEmpty());
//	         for (String string : maxList) {  
//	             map.put(string, 1);  
//	         }  
//	         System.out.println(map.isEmpty());
//	         for (String string : maxList) {  
//	             map.remove(string, 1);  
//	         }  
//	         System.out.println(map.isEmpty());
	         Map<String,String> map1 = new HashMap<String,String>((int)(maxList.size()/0.75));
	         System.out.println(map1.size());
	         for (String string : maxList) {  
	             map1.put(string, string+"haha"); 
	             map1.get(string);
	         }  
	         System.out.println(map1.isEmpty());
	         for (String string : minList) {  
	             if(map1.get(string)!=null)  
	             {  
	            	 //System.out.println(map1.get(string));
	            	 //result = result +","+string;
	             }  
	         }  
	         System.out.println("getDiffrent5 total times "+(System.nanoTime()-st));  
	}

}
