package ch.rgw.elexis.docmgr_lucinda.view;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import ch.rgw.elexis.docmgr_lucinda.Activator;
import ch.rgw.elexis.docmgr_lucinda.model.Document;
import ch.rgw.lucinda.Handler;

public class LucindaMessages extends ViewPart implements Handler {
	private TreeViewer tv;
	public LucindaMessages() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		tv=new TreeViewer(parent);
		tv.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			
			@Override
			public void dispose() {}
			
			@Override
			public boolean hasChildren(Object element) {
				if(element instanceof Document){
					return true;
				}else{
					return false;
				}
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return Activator.getDefault().getMessages().toArray();
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				Document doc=(Document) parentElement;
				Set<Entry<String,Object>> entries=doc.toMap().entrySet();
				return entries.toArray();
			}
		});
			
		tv.setLabelProvider(new LabelProvider(){

			@Override
			public String getText(Object element) {
				if(element instanceof Document){
					return ((Document)element).get("status"); //$NON-NLS-1$
				}else if(element instanceof Entry){
					Entry e=(Entry)element;
					return e.getKey()+": "+e.getValue(); //$NON-NLS-1$
				}else{
					return "?"; //$NON-NLS-1$
				}

			}
			
		});
		Activator.getDefault().addHandler(this);
		
		tv.setInput(Activator.getDefault().getMessages());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void signal(Map<String, Object> msg) {
		Display.getDefault().asyncExec(new Runnable(){

			@Override
			public void run() {
				tv.add("/", new Document(msg)); //$NON-NLS-1$
			}
			
		});
		
	}

}
