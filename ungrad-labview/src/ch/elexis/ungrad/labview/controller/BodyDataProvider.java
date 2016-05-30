package ch.elexis.ungrad.labview.controller;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.LabResultsRow;

public class BodyDataProvider implements IDataProvider {
	LabResultsRow[] labValues = new LabResultsRow[0];
	private Controller controller;

	public BodyDataProvider(Controller controller) {
		this.controller = controller;
	}

	public void setPatient(Patient pat) {
		labValues = (LabResultsRow[]) controller.loadData(pat).keySet().toArray();
	}

	@Override
	public int getColumnCount() {
		return labValues.length;
	}

	@Override
	public Object getDataValue(int row, int column) {
		LabResultsRow lrow = labValues[row];
		return lrow.get(column);
	}

	@Override
	public int getRowCount() {
		return labValues.length;
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

}
