package ch.elexis.ungrad.labview.controller.smart;

import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;

public class SmartViewController implements IObserver {

	Controller controller;
	
	SmartContentProvider scp;
	public SmartViewController(Controller parent) {
		controller=parent;
		scp=new SmartContentProvider(controller.getLRS());
		
	}
	@Override
	public void signal(Object message) {

	}

}
