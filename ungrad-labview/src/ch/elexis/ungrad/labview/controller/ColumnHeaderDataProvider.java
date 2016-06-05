package ch.elexis.ungrad.labview.controller;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class ColumnHeaderDataProvider implements IDataProvider {
	Controller controller;

	public ColumnHeaderDataProvider(Controller controller) {
		this.controller = controller;
	}

	@Override
	public int getColumnCount() {
		int col = controller.labResults.getColumnCount();
		return col == 0 ? 1 : col+1;
	}

	@Override
	public Object getDataValue(int row, int column) {
		Object res = controller.labResults.getDates()[column];
		return res == null ? Integer.toString(column) : res;
		
	}

	@Override
	public int getRowCount() {
		return 1; //controller.labResults.getRowCount();
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

}
