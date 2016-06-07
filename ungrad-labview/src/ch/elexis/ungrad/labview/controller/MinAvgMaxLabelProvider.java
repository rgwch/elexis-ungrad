package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.model.Bucket;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class MinAvgMaxLabelProvider extends StyledCellLabelProvider {
	LabTableColumns parent;
	LabResultsSheet sheet;
	TextStyle smaller;
	TextStyle normal;
	Color black=Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	Color white=Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	Color red=UiDesk.getColor(UiDesk.COL_RED); //Display.getDefault().getSystemColor(SWT.COLOR_RED);

	
	public MinAvgMaxLabelProvider(LabTableColumns parent) {
		this.parent = parent;
		sheet = parent.getLabResultsSheet();
		normal=new TextStyle(parent.getDefaultFont(),black,white);
		smaller=new TextStyle(parent.getSmallerFont(),black,white);
	}

	@Override
	public void update(ViewerCell cell) {
		LabResultsRow results = (LabResultsRow) cell.getElement();
		Item item = results.getItem();
		Bucket bucket = null;
		switch (cell.getColumnIndex()) {
		case LabTableColumns.COL_RECENT:
			bucket = sheet.getRecentBucket(item);
			break;
		case LabTableColumns.COL_LASTYEAR:
			bucket = sheet.getOneYearBucket(item);
			break;
		case LabTableColumns.COL_OLDER:
			bucket = sheet.getOlderBucket(item);
			break;
		default:
			cell.setText("??");
			super.update(cell);
			return;
		}
		if (bucket != null) {
			StringBuilder text=new StringBuilder(bucket.getMinResult());
			StyleRange[] ranges=new StyleRange[3];
			ranges[0]=new StyleRange(0,text.length(),black,white);
			ranges[0].font=parent.getSmallerFont();
			text.append(" - ");
			String avg=bucket.getAverageResult();
			ranges[1]=new StyleRange(text.length(),avg.length(),black,white);
			ranges[1].font=parent.getDefaultFont();
			text.append(avg).append(" - ");
			String max=bucket.getMaxResult();
			ranges[2]=new StyleRange(text.length(),max.length(),black,white);
			ranges[2].font=parent.getSmallerFont();
			text.append(max);
			cell.setText(text.toString());
			cell.setStyleRanges(ranges);
			
			
		}
		super.update(cell);
	}

}
