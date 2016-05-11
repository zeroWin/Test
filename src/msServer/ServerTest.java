package msServer;

import java.util.*;


class Field{//***
	public String FId;
	public Field(String FId){
		this.FId=FId;
	}
	public String toString(){
		return FId;
	}
}

public class ServerTest{
	static List<Field> findSameElements(List<Field> lst1,List<Field> lst2){
		class FieldComparator implements Comparator<Field>{//***
			public int compare(Field obj1,Field obj2){
				String s1=obj1.FId,s2=obj2.FId;
				int dex1=0,dex2=0;
				while(dex1<s1.length()&&dex2<s2.length()){
					if(s1.charAt(dex1)<s2.charAt(dex2)) return -1;
					if(s1.charAt(dex1)>s2.charAt(dex2)) return 1;
					dex1++;dex2++;
				}
				if(dex1<s1.length())return 1;
				else if(dex2<s2.length()) return -1;
				return 0;
			}
		}
		//排序
		Collections.sort(lst1,new FieldComparator());
		Collections.sort(lst2,new FieldComparator());
		
		List<Field> res=new ArrayList<Field>();
		FieldComparator cmp=new FieldComparator();
		int iter1=0,iter2=0;
		while(iter1<lst1.size()&&iter2<lst2.size()){
			int resCompare=cmp.compare(lst1.get(iter1), lst2.get(iter2));
			if(resCompare==1) iter2++;
			else if(resCompare==-1) iter1++;
			else{
				res.add(lst1.get(iter1));
				iter1++;iter2++;
			}
		}
		return res;		
	}
	
	public static void main(String[] args){
		List<Field> list1=new ArrayList<Field>();
		List<Field> list2=new ArrayList<Field>();

		for (int i = 0; i < 10000; i++) {  
            list1.add(new Field("test"+i));  
            list2.add(new Field("test"+i*2));  
        }  
		
		long start1=System.nanoTime();
		long start2=System.currentTimeMillis();

		List<Field> same=findSameElements(list1,list2);
		long end1=System.nanoTime();
		long end2=System.currentTimeMillis();

		System.out.println("Runing time : "+(end1-start1)+" ns");
		System.out.println("Runing time : "+(end2-start2)+" ms");

		String string = "1234215125124124";
		string = string.replace("1", "xxx");
		System.out.println(string);
		System.out.println(same);

	}
}




