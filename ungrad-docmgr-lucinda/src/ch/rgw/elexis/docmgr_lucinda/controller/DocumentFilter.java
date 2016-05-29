package ch.rgw.elexis.docmgr_lucinda.controller;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.rgw.elexis.docmgr_lucinda.Preferences;
import ch.rgw.elexis.docmgr_lucinda.model.Document;

public class DocumentFilter extends ViewerFilter {
	private Set<String> doctypes = new HashSet<>();
	
	public void add(String doctype){
		doctypes.add(doctype);
	}
	
	public void remove(String doctype){
		doctypes.remove(doctype);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		Document doc=new Document(element);
		return doctypes.contains(doc.get(Preferences.FLD_LUCINDA_DOCTYPE)); //$NON-NLS-1$
	}
	
}
