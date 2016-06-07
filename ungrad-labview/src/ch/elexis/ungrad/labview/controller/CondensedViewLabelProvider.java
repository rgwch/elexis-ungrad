/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
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
package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.ungrad.labview.model.Bucket;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * An OwnerDraw LablProvider to display lab results in the "coondensed" view. There is a column for most recent lab results, for such older than a month
 * and for such older than a year. For every group, min, max and avg values are shown-
 * @author gerry
 *
 */
public class CondensedViewLabelProvider extends OwnerDrawLabelProvider {
	final int padding = 4;
	Font standard;
	Font smaller;
	LabTableColumns ltc;
	int columnIndex;
	LabResultsSheet sheet;
	Color black, red;

	public CondensedViewLabelProvider(LabTableColumns ltc, int column) {
		standard = ltc.getDefaultFont();
		smaller = ltc.getSmallerFont();
		this.ltc = ltc;
		columnIndex = column;
		sheet = ltc.getLabResultsSheet();
		black = UiDesk.getColor(UiDesk.COL_BLACK);
		red = UiDesk.getColor(UiDesk.COL_RED);
	}

	@Override
	protected void measure(Event event, Object element) {
		float h = standard.getFontData()[0].height;
		int w = 200;
		event.setBounds(new Rectangle(event.x, event.y, w, Math.round(1.5f * h)));
	}

	@Override
	protected void paint(Event event, Object element) {
		LabResultsRow results = (LabResultsRow) element;
		Item item = results.getItem();
		Bucket bucket = null;
		switch (columnIndex) {
		case LabTableColumns.COL_RECENT:
			bucket = sheet.getRecentBucket(item);
			break;
		case LabTableColumns.COL_LASTYEAR:
			bucket = sheet.getOneYearBucket(item);
			break;
		case LabTableColumns.COL_OLDER:
			bucket = sheet.getOlderBucket(item);
			break;
		default:
			Rectangle ct = centerText(event.gc, event.getBounds(), "??");
			event.gc.drawText("??", ct.x, ct.y);
			return;
		}
		if (bucket != null) {
			GC gc = event.gc;
			if (item.titel.equals("WBC")) {
				@SuppressWarnings("unused")
				int x = 0;

			}
			Rectangle bounds = event.getBounds();
			bounds.width = ltc.getColumnWidth(columnIndex);
			String avg = bucket.getAverageResult();
			gc.setFont(standard);
			Rectangle rCenter = centerText(gc, bounds, avg);
			gc.setForeground(bucket.isPathologic(avg) ? red : black);
			gc.drawText(avg, bounds.x + rCenter.x, bounds.y + rCenter.y);
			if (bucket.getResultCount() > 1) {
				gc.setFont(smaller);
				String right = bucket.getMaxResult();
				Point ptRight = gc.textExtent(right);
				int yOffset = rCenter.height - ptRight.y;
				int xOffset = bounds.width - ptRight.x - padding;
				gc.setForeground(bucket.isPathologic(right) ? red : black);
				gc.drawText(right, bounds.x + xOffset, bounds.y + rCenter.y + yOffset);
				String left = bucket.getMinResult();
				gc.setForeground(bucket.isPathologic(left) ? red : black);
				gc.drawText(left, bounds.x + padding, bounds.y + rCenter.y + yOffset);
			}
		}
	}

	Rectangle centerText(GC gc, Rectangle bounds, String text) {
		Point pt = gc.stringExtent(text);
		float yoffs = ((float) bounds.height - (float) pt.y) / 2f;
		float xoffs = ((float) bounds.width - (float) pt.x) / 2f;
		return new Rectangle(Math.round(xoffs), Math.round(yoffs), pt.x, pt.y);
	}

}
