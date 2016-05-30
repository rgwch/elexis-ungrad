package ch.elexis.ungrad.labview.views;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.ungrad.labview.controller.Controller;

public class LaborView extends ViewPart{
	Controller controller=new Controller();
	NatTable nat;
	
	@Override
	public void createPartControl(Composite parent) {
		nat=new NatTable(parent,controller.getBaseLayer());
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
