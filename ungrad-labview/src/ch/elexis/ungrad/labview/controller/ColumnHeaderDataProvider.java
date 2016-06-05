package ch.elexis.ungrad.labview.controller;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class ColumnHeaderDataProvider implements IDataProvider {
	Controller controller;
	
	public ColumnHeaderDataProvider(Controller controller) {
		this.controller=controller;
	}
	@Override
	public int getColumnCount() {
		return controller.labResults.getColumnCount();
	}

	@Override
	public Object getDataValue(int row, int column) {
		return controller.labResults.getDates()[column];
	}

	@Override
	public int getRowCount() {
		return controller.labResults.getRowCount();
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

}
