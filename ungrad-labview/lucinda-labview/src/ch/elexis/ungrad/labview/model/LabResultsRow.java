package ch.elexis.ungrad.labview.model;

import java.util.SortedSet;
import java.util.TreeSet;

public class LabResultsRow {
	public LabResultsRow(Item item){
		this.item=item;
		results=new TreeSet<Result>();
	}
	public void add(Result result){
		results.add(result);
	}
	public Result get(int index){
		if(results.size()>index){
			return (Result) (results.toArray()[index]);
		}else{
			return null;
		}
	}
	Item item;
	SortedSet<Result> results;
}
