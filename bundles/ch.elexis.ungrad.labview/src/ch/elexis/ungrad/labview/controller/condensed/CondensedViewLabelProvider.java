/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/
package ch.elexis.ungrad.labview.controller.condensed;

import java.util.Optional;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.model.Bucket;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * An OwnerDraw LablProvider to display lab results in the "coondensed" view. There is a column for
 * most recent lab results, for such older than a month and for such older than a year. For every
 * group, min, max and avg values are shown-
 * 
 * @author gerry
 * 		
 */
public class CondensedViewLabelProvider extends OwnerDrawLabelProvider {
	final int padding = 8;
	// Font standard;
	// Font smaller;
	LabSummaryTreeColumns ltc;
	int columnIndex;
	Color black, red;
	private IContextService contextService=ContextServiceHolder.get();
	
	public CondensedViewLabelProvider(LabSummaryTreeColumns ltc, int column){
		// standard = ltc.getDefaultFont();
		// smaller = ltc.getSmallerFont();
		this.ltc = ltc;
		columnIndex = column;
		black = UiDesk.getColor(UiDesk.COL_BLACK);
		red = UiDesk.getColor(UiDesk.COL_RED);
	}
	
	@Override
	protected void measure(Event event, Object element){
		float h = ltc.getDefaultFont().getFontData()[0].height;
		int w = 200;
		event.setBounds(new Rectangle(event.x, event.y, w, Math.round(1.5f * h)));
	}
	
	@Override
	protected void paint(Event event, Object element){
		Optional<IPatient> actPat = contextService.getActivePatient();
		if (element instanceof LabResultsRow) {
			LabResultsSheet sheet = ltc.getLabResultsSheet();
			LabResultsRow results = (LabResultsRow) element;
			Item item = results.getItem();
			Bucket bucket = null;
			switch (columnIndex) {
			case LabSummaryTreeColumns.COL_RECENT:
				bucket = sheet.getRecentBucket(item);
				break;
			case LabSummaryTreeColumns.COL_LASTYEAR:
				bucket = sheet.getOneYearBucket(item);
				break;
			case LabSummaryTreeColumns.COL_OLDER:
				bucket = sheet.getOlderBucket(item);
				break;
			default:
				Rectangle ct = centerText(event.gc, event.getBounds(), "??");
				event.gc.drawText("??", ct.x, ct.y);
				return;
			}
			if (bucket != null && actPat.isPresent()) {
				GC gc = event.gc;
				Rectangle bounds = event.getBounds();
				bounds.width = ltc.getColumnWidth(columnIndex);
				String avg = bucket.getAverageResult();
				gc.setFont(ltc.getDefaultFont());
				Rectangle rCenter = centerText(gc, bounds, avg);
				gc.setForeground(item.isPathologic(actPat.get(), avg) ? red : black);
				//gc.setBackground(((TreeItem)event.item)..getBackground(columnIndex));
				
				gc.drawText(avg, bounds.x + rCenter.x, bounds.y + rCenter.y);
				if (bucket.getResultCount() > 1) {
					gc.setFont(ltc.getSmallerFont());
					String right = bucket.getMaxResult();
					Point ptRight = gc.textExtent(right);
					int yOffset = rCenter.height - ptRight.y;
					// int xOffset = (bounds.width - ptRight.x - padding);
					float trailingSpace =
						(float) bounds.width - (float) rCenter.width - (float) rCenter.x;
					float remainingSpace = trailingSpace - (float) ptRight.x;
					float xOffset = (float) bounds.x + (float) rCenter.x + (float) rCenter.width
						+ (remainingSpace / 2f);
					gc.setForeground(item.isPathologic(actPat.get(), right) ? red : black);
					gc.drawText(right, Math.round(xOffset), bounds.y + rCenter.y + yOffset);
					String left = bucket.getMinResult();
					Point ptLeft = gc.stringExtent(left);
					float leadingSpace = rCenter.x - ptLeft.x;
					xOffset = leadingSpace / 2;
					gc.setForeground(item.isPathologic(actPat.get(), left) ? red : black);
					gc.drawText(left, Math.round((float) bounds.x + xOffset),
						bounds.y + rCenter.y + yOffset);
				}
			}
		} else {
			Rectangle bounds = event.getBounds();
			bounds.width = ltc.getColumnWidth(columnIndex);
			event.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			event.gc.fillRectangle(bounds);
		}
	}
	
	Rectangle centerText(GC gc, Rectangle bounds, String text){
		Point pt = gc.stringExtent(text);
		float yoffs = ((float) bounds.height - (float) pt.y) / 2f;
		float xoffs = ((float) bounds.width - (float) pt.x) / 2f;
		return new Rectangle(Math.round(xoffs), Math.round(yoffs), pt.x, pt.y);
	}
	
}
