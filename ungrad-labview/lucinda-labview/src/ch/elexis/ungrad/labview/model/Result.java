package ch.elexis.ungrad.labview.model;

public class Result implements Comparable<Result>{

	String itemId, date,time,result,comment;
	
	public Result(String id, String dat,String zeit, String res, String com){
		itemId=id;
		date=dat;
		time=zeit;
		result=res;
		comment=com;
	}
	@Override
	public int compareTo(Result o) {
		if(date.equals(o.date)){
			return time.compareTo(o.time);
		}else{
			return date.compareTo(o.date);
		}
	}
	
	
}
