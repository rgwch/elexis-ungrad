package ch.elexis.ungrad.inbox.model;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;

public class Controller extends TableLabelProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object dirname) {
		File dir = new File((String) dirname);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if(files!=null) {
				return files;
			}	
		}
		return new File[0];
	}

	/* LabelProvider */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		return ((File) element).getName();
	}

}
