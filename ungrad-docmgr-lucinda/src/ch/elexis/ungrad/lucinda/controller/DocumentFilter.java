package ch.elexis.ungrad.lucinda.controller;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.ungrad.lucinda.Preferences;
import io.vertx.core.json.JsonObject;

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
		JsonObject doc = (JsonObject) element;
		return doctypes.contains(doc.getString(Preferences.FLD_LUCINDA_DOCTYPE)); // $NON-NLS-1$
	}

}
