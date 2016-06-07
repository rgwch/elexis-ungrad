package ch.elexis.ungrad.labview.controller;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.ungrad.labview.model.Bucket;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class CondensedViewLabelProvider extends OwnerDrawLabelProvider {
	final int padding = 4;
	Font standard;
	Font smaller;
	LabTableColumns ltc;
	int columnIndex;
	LabResultsSheet sheet;

	public CondensedViewLabelProvider(LabTableColumns ltc, int column) {
		standard = ltc.getDefaultFont();
		smaller = ltc.getSmallerFont();
		this.ltc = ltc;
		columnIndex = column;
		sheet = ltc.getLabResultsSheet();
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
			gc.drawText(avg, bounds.x + rCenter.x, bounds.y + rCenter.y);
			if (bucket.getResultCount() > 1) {
				gc.setFont(smaller);
				String right = bucket.getMaxResult();
				Point ptRight = gc.textExtent(right);
				int yOffset = rCenter.height - ptRight.y;
				int xOffset = bounds.width - ptRight.x - padding;
				gc.drawText(right, bounds.x + xOffset, bounds.y + rCenter.y + yOffset);
				String left = bucket.getMinResult();
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
