package ch.elexis.ungrad.labview.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.*;

public class Controller {
	LabResultsSheet labResultGenerator;
	
	public Controller(){
		labResultGenerator=new LabResultsSheet();
	}
	
	public SortedMap<Item,LabResultsRow> loadData(Patient pat){
		try {
			return labResultGenerator.fetch(pat);
		} catch (ElexisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new TreeMap<Item,LabResultsRow>();
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
