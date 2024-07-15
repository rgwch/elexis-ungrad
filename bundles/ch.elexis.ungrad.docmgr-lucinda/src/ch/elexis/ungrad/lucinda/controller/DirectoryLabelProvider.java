package ch.elexis.ungrad.lucinda.controller;

import java.io.File;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.rgw.tools.TimeTool;

public class DirectoryLabelProvider extends TableLabelProvider {

	@Override
	public String getColumnText(Object element, int col) {
		File file = (File) element;
		if (col == 0) {
			return new TimeTool(file.lastModified()).toString(TimeTool.DATE_GER);
		} else if (col == 1) {
			return file.getName();
		} else {
			return "?";
		}
	}
}
