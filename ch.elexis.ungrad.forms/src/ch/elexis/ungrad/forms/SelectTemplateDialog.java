/**
 * 
 */
package ch.elexis.ungrad.forms;

import java.io.File;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Brief;
import ch.rgw.tools.StringTool;

/**
 * @author gerry
 *
 */
public class SelectTemplateDialog extends TitleAreaDialog {

	TableViewer tv;
	String result;
	
	public SelectTemplateDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		tv = new TableViewer(ret, SWT.V_SCROLL);
		tv.setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object type) {
				File dir = new File(CoreHub.localCfg.get(PreferenceConstants.TEMPLATES, ""));
				String[] templates = dir.list();
				if (templates == null) {
					return new String[0];
				} else {
					return templates;
				}
			}
		});
		tv.setLabelProvider(new DefaultLabelProvider());
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Formular auswählen");
	}
	
	@Override
	protected void okPressed(){
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if ((sel != null) && (!sel.isEmpty())) {
			result = (String) sel.getFirstElement();
		}
		super.okPressed();
	}
	

}
