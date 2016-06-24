package ch.elexis.ungrad.labview.controller.smart;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

public class SmartSummaryLabelProvider extends StyledCellLabelProvider {
	Controller ctl;
	TimeTool limit;

	public SmartSummaryLabelProvider(Controller ctl, TimeTool before) {
		this.ctl = ctl;
		limit = before;
	}

	@Override
	public void update(ViewerCell cell) {
		if (cell.getElement() instanceof LabResultsRow) {
			LabResultsRow lr = (LabResultsRow) cell.getElement();
			Result[] minmax = lr.getBoundsBefore(limit);
			StringBuilder result = new StringBuilder("");
			if (minmax != null && minmax[0]!=null) {
				result.append(minmax[0].get("resultat"));
				if (!minmax[0].equals(minmax[1]) && minmax[1]!= null) {
					result.append("-")
					.append(minmax[1].get("resultat"));
				}
			}
			cell.setText(result.toString());
			super.update(cell);
		}
	}

}
