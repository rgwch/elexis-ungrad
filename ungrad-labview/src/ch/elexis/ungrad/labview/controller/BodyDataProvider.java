package ch.elexis.ungrad.labview.controller;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ch.elexis.ungrad.labview.model.Result;

public class BodyDataProvider implements IDataProvider {
	private Controller controller;

	public BodyDataProvider(Controller controller) {
		this.controller = controller;
	}

	@Override
	public int getColumnCount() {
		int col = controller.labResults.getColumnCount();
		return col;
	}

	@Override
	public Object getDataValue(int row, int column) {
		String res="?";
		if (column == 0) {
			res=controller.labResults.getItemAt(row).toString();
		} else {
			res = controller.labResults.getValue(row, column).toString();
			if (res == null) {
				res=Integer.toString(row) + "," + Integer.toString(column);
			}
		}
		return res;
	}

	@Override
	public int getRowCount() {
		int row = controller.labResults.getRowCount();
		return row == 0 ? 10 : row;
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

}
