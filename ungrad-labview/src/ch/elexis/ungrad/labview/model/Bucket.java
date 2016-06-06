package ch.elexis.ungrad.labview.model;

import java.util.ArrayList;
import java.util.List;

import ch.rgw.tools.StringTool;

public class Bucket {
	Item item;
	List<Result> results=new ArrayList<Result>();
	
	Bucket(Item item){
		this.item=item;
	}
	void addResult(Result result){
		results.add(result);
	}
	
	public String getMinResult(){
		float cmp=Float.MAX_VALUE;
		for(Result result:results){
			float val=makeFloat(result.result);
			if(val<cmp){
				cmp=val;
			}
		}
		return Float.toString(cmp);
	}
	
	public String getMaxResult(){
		float cmp=0;
		for(Result result:results){
			float val=makeFloat(result.result);
			if(val>cmp){
				cmp=val;
			}
		}
		return Float.toString(cmp);
	}
	
	public String getAverageResult(){
		int num=0;
		float sum=0;
		for(Result result:results){
			sum+=makeFloat(result.result);
			num+=1;
		}
		float avg=100*sum/num;
		float ret=((float)Math.round(avg))/100f;
		return Float.toString(ret);
	}
	
	float makeFloat(String s){
		String[] splitted=s.split("[\\.,]");
		if(splitted[0].matches("[0-9]+")){
			String einer=splitted[0];
			String frac="0";
			if(splitted.length>2){
				return -1.0f;
			}if(splitted.length==2){
				if(splitted[1].matches("[0-9]+")){
					frac=splitted[1];
				}else{
					return -1.0f;
				}
			}
			frac=StringTool.pad(StringTool.RIGHTS, '0', frac, 3);
			float fr=((float)Integer.parseInt(frac))/1000f;
			float ret =Integer.parseInt(einer)+fr;
			return ret;
		}else{
			return -1.0f;
		}
	}
}
