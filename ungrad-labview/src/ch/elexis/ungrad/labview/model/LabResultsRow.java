package ch.elexis.ungrad.labview.model;

import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.data.Patient;

public class LabResultsRow {
	Item item;
	Patient patient;
	SortedSet<Result> results;

	public LabResultsRow(Item item, Patient pat){
		this.item=item;
		results=new TreeSet<Result>();
		this.patient=pat;
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
	public Item getItem(){
		return item;
	}
	public Patient getPatient() {
		return patient;
	}

}
