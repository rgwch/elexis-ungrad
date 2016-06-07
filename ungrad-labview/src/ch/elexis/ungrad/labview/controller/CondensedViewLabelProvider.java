package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.model.LabResultsRow;

public class CondensedViewLabelProvider extends OwnerDrawLabelProvider {
	Font standard;
	Font smaller;
	LabTableColumns ltc;
	
	public CondensedViewLabelProvider(LabTableColumns ltc) {
		standard=ltc.getDefaultFont();
		smaller=ltc.getSmallerFont();
		this.ltc=ltc;
	}

	@Override
	protected void measure(Event event, Object element) {
		float h=standard.getFontData()[0].height;
		int w=100;
		event.setBounds(new Rectangle(event.x,event.y,w,Math.round(h+h/5f)));
	}

	@Override
	protected void paint(Event event, Object element) {
		Rectangle bounds=event.getBounds();
		event.gc.setFont(smaller);
		event.gc.drawText("hallo", bounds.x+3, bounds.y+3);
		event.gc.setFont(standard);
		event.gc.drawText("ecli", bounds.x+10, bounds.y+4);
	}

}
