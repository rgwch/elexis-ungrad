package ch.elexis.ungrad.labview.controller;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class Controller {
	LabResultsSheet labResults;
	
	public Controller(){
		labResults=new LabResultsSheet();
	}
	
	public void setPatient(Patient pat){
		try {
			labResults.setPatient(pat);
		} catch (ElexisException e) {
			e.printStackTrace();
		}
	}

	public ILayer getBaseLayer(){
		return new DefaultGridLayer(getBodyDataProvider(), getColumnHeaderDataProvider());
	}
	
	public IDataProvider getBodyDataProvider(){
		return new BodyDataProvider(this);
	}
	
	public IDataProvider getColumnHeaderDataProvider(){
		return new ColumnHeaderDataProvider(this);
	}
}
