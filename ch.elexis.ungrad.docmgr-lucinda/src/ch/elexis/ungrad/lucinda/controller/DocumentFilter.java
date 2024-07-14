package ch.elexis.ungrad.lucinda.controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.ungrad.lucinda.Preferences;


public class DocumentFilter extends ViewerFilter {
	private Set<String> doctypes = new HashSet<>();

	public void add(String doctype) {
		doctypes.add(doctype);
	}

	public void remove(String doctype) {
		doctypes.remove(doctype);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Map doc = (Map)element;
		return doctypes.contains(doc.get(Preferences.FLD_LUCINDA_DOCTYPE)); // $NON-NLS-1$
	}

}
