package ch.elexis.ungrad.labview.controller.full;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.ungrad.labview.model.Item;

public class LabItemPropertiesDlg extends TitleAreaDialog {
	Item item;
	LabItem li;
	String[] fields={LabItem.TITLE,LabItem.SHORTNAME,LabItem.GROUP,LabItem.PRIO};
	String[] labels={"Name","Kürzel","Gruppe","Priorität"};
	Text[] textfields=new Text[fields.length];
	
	public LabItemPropertiesDlg(Item item) {
		super(Hub.getActiveShell());
		this.item=item;
		li=LabItem.load(item.get("id"));
	}

	
	@Override
	protected void okPressed() {
		String[] values=new String[textfields.length];
		for(int i=0;i<values.length;i++){
			values[i]=textfields[i].getText();
		}
		li.set(fields, values);
		super.okPressed();
	}


	@Override
	public void create() {
		super.create();
		Bundle bundle=FrameworkUtil.getBundle(getClass());
		URL url=FileLocator.find(bundle, new Path("icons/lab.png"), null);
		//setTitleImage(ImageDescriptor.createFromURL(url).createImage());
		setTitle("Eigenschaften Labor Item");
		setMessage("Eigenschaft anpassen");
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret=new Composite(parent,SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2,false));

		for(int i=0;i<fields.length;i++){
			new Label(ret, SWT.NONE).setText(labels[i]);
			textfields[i]=new Text(ret,SWT.SINGLE);
			textfields[i].setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
			textfields[i].setText(li.get(fields[i]));
		}
		
		return ret;
	}

	
	
	
}

