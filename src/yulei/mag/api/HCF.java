package yulei.mag.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yulei.mag.api.APIuse;
import yulei.mag.api.ResultJsonClass;
import yulei.mag.api.ResultJsonClass.Author;
import yulei.mag.api.ResultJsonClass.Entities;
import yulei.mag.api.ResultJsonClass.Field;
import yulei.mag.api.ReturnResult;

public class HCF {
	public static APIuse apiuse;
	public static ReturnResult returnResult;
	
	public HCF(){
		apiuse = new APIuse();
	};
	

	public static String addPath(String res,String newPath){
		if(res==""&&newPath=="")return res;
		if(res=="") return newPath;
		if(newPath=="") return res;
		return res+','+newPath;
	}
	
	/*     以下为IdToAuid的方法         */
	/**
	 * 
	 * @param id1
	 * @param audi2
	 * @return
	 */
	public String IdToAuid_All(String id1,String auid2){
//    	return IdToAuid_1Hop(id1,auid2)+IdToAuid_2Hop(id1,auid2)+IdToAuid_3Hop(id1,auid2);
		return IdToAuid_1Hop(id1,auid2);
    }
    public String IdToAuid_1Hop(String id1,String auid2){
    	apiuse.setCount("1");
		apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		apiuse.setOffset("0");
		
		String expr = "Id="+id1;
		apiuse.setExpr(expr);
				
		// Get id1 search result
        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());
        if(searchResultId.entities!=null&&searchResultId.entities.size()!=0){
        	List<Author> lstAuthor=searchResultId.entities.get(0).AA;
            for(Author author:lstAuthor){
            	if(author.AuId==auid2)
            		return "["+id1+","+author+"]";
            }
        }
        return "";	
    }
    /**
     * situation1:id1->id1.Rid->audi2
     * situation2:id1->id1.Fid->audi2
     * @param id1
     * @param audi2
     * @return
     */
    public String IdToAuid_2Hop(String id1,String auid2){
    	apiuse.setCount("10000");
		apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		apiuse.setOffset("0");
		
		String expr = "Id="+id1;
		apiuse.setExpr(expr);
        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());

        String res="";
        
    	/*  situation1:id1->id1.Rid->audi2  */
        if(searchResultId.entities!=null&&searchResultId.entities.size()!=0){
        	StringBuilder sb=new StringBuilder();
    		List<String> lstRid=searchResultId.entities.get(0).RId;
    		for(String rid:lstRid){
    			String cur=IdToAuid_1Hop(rid,auid2);
    			if(cur!="") sb.append(cur+',');
    		}
        	if(sb.length()>0)sb.delete(sb.length()-1,sb.length());
        	addPath(res,sb.toString());
        }
        
        /*  situation2:id1->id1.Fid->audi2  */
        if(searchResultId.entities!=null&&searchResultId.entities.size()!=0){
        	StringBuilder sb=new StringBuilder();
    		List<Field> lstFid=searchResultId.entities.get(0).F;
    		for(Field field:lstFid){
    			String cur=AuidToFid_1Hop(auid2,field.FId);
    			if(cur!="") sb.append("cur"+',');
    		}
    		if(sb.length()>0)sb.delete(sb.length()-1,sb.length());
        	addPath(res,sb.toString());
        }
        return res;
    }
    public String IdToAuid_3Hop(String id1,String audi2){
    	return "";
    }
    
    
    public String AuidToFid_1Hop(String auid1,String fid2){
    	apiuse.setCount("10000");
		apiuse.setAttributes("Id,F.FId,J.JId,C.CId,AA.AuId,AA.AfId,RId");
		apiuse.setOffset("0");
		
		String expr = "Composite(AA.AuId="+auid1+")";
		apiuse.setExpr(expr);
        ResultJsonClass searchResultId = apiuse.HandleURI(apiuse.getURI());

        if(searchResultId.entities!=null&&searchResultId.entities.size()!=0){
        	List<Field> lstFid=searchResultId.entities.get(0).F;
    		for(Field field:lstFid){
    			if(field.FId==fid2)
    				return "["+auid1+","+fid2+"]";
    		}
        }
        return "";
    }
	
	public static void main(String[] args){
		HCF hcf=new HCF();
		System.out.println(hcf.IdToAuid_All("2180737804", "2251253715"));
	}
	
}
