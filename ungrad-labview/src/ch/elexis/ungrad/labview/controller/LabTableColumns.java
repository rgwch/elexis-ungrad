package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class LabTableColumns {
	public static final int COL_RECENT = 2;
	public static final int COL_LASTYEAR = 3;
	public static final int COL_OLDER = 4;

	private LabTableColumn[] cols;
	private LabResultsSheet sheet;
	private Font smallerFont;

	public LabTableColumns(LabResultsSheet sheet, TableViewer tv, int num) {
		this.sheet = sheet;
		cols = new LabTableColumn[num];
		for (int i = 0; i < num; i++) {
			cols[i] = new LabTableColumn(tv);
		}
		Display display = Display.getDefault();
		FontData[] fontData = display.getSystemFont().getFontData();
		for (int i = 0; i < fontData.length; ++i) {
			float h = fontData[i].getHeight() * 3 / 4;
			fontData[i].setHeight(Math.round(h));
		}
		smallerFont = new Font(display, fontData);
	}

	public void dispose() {
		smallerFont.dispose();
	}

	public LabResultsSheet getLabResultsSheet() {
		return sheet;
	}

	public Font getDefaultFont(){
		return Display.getDefault().getSystemFont();
	}
	public Font getSmallerFont() {
		return smallerFont;
	}

	public void reload(LabContentProvider lcp) {
		cols[0].setLabel("Parameter");
		cols[0].setWidth(150);
		cols[0].setLabelProvider(new ItemTextLabelProvider());
		cols[1].setLabel("Normbereich");
		cols[1].setWidth(150);
		cols[1].setLabelProvider(new ItemRangeLabelProvider());
		String mode = Preferences.cfg.get(Preferences.MODE, "compact");
		switch (mode) {
		case "compact":
			reloadCompact(lcp);
			break;
		}
	}

	private void reloadCompact(LabContentProvider lcp) {
		cols[COL_RECENT].setLabel("aktuell");
		cols[COL_RECENT].setWidth(100);
		cols[COL_RECENT].setLabelProvider(new MinAvgMaxLabelProvider(this));
		cols[COL_LASTYEAR].setLabel("letzte 12 Monate");
		cols[COL_LASTYEAR].setWidth(100);
		cols[COL_LASTYEAR].setLabelProvider(new MinAvgMaxLabelProvider(this));
		cols[COL_OLDER].setLabel("älter");
		cols[COL_OLDER].setWidth(100);
		cols[COL_OLDER].setLabelProvider(new MinAvgMaxLabelProvider(this));
		for (int i = 5; i < cols.length; i++) {
			cols[i].setWidth(0);
			cols[i].setLabelProvider(new NullLabelProvider());
		}
	}
}
