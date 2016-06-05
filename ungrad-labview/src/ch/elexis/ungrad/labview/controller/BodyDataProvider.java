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
		return controller.labResults.getColumnCount();
	}

	@Override
	public Object getDataValue(int row, int column) {
		Result res = controller.labResults.getValue(row, column);
		return res;
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
