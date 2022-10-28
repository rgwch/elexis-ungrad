package ch.elexis.ungrad.forms;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Patient;

public class Controller extends TableLabelProvider implements IStructuredContentProvider {
	private Patient currentPatient;

	void changePatient(Patient pat) {
		currentPatient = pat;
	}

	File getOutputDirFor(Patient p) {
		String name = p.getName();
		String fname = p.getVorname();
		String birthdate = p.getGeburtsdatum();
		File superdir = new File(CoreHub.localCfg.get(PreferenceConstants.OUTPUT, ""), name.substring(0, 1).toLowerCase());
		File dir = new File(superdir, name + "_" + fname + "_" + birthdate);
		return dir;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Patient pat = (Patient) inputElement;
		File dir = getOutputDirFor(pat);
		String[] files = dir.list();
		if (files == null) {
			return new String[0];
		} else {
			return files;
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		return (String) element;
	}
	
	public void createDocumentFrom(String template) {
		
	}
}
