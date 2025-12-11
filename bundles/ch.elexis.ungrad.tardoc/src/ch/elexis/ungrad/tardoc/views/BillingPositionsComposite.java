package ch.elexis.ungrad.tardoc.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Composite for displaying billing positions with a table viewer.
 * This component is extracted from TardocKonsView to allow better separation of concerns
 * and to enable show/hide functionality.
 */
public class BillingPositionsComposite extends Composite {

	private TableViewer billingPositionsViewer;
	private Group billingGroup;

	/**
	 * Label provider for billing positions
	 */
	class BillingPositionLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		
		@Override
		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

	/**
	 * Creates a new billing positions composite.
	 * 
	 * @param parent the parent composite
	 * @param style the SWT style bits
	 */
	public BillingPositionsComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	/**
	 * Creates the content of this composite
	 */
	private void createContent() {
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		billingGroup = new Group(this, SWT.NONE);
		billingGroup.setText("Billing Positions");
		billingGroup.setLayout(new GridLayout(1, false));
		billingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		billingPositionsViewer = new TableViewer(billingGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		billingPositionsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		billingPositionsViewer.getTable().setHeaderVisible(true);
		billingPositionsViewer.getTable().setLinesVisible(true);

		// Set content and label provider
		billingPositionsViewer.setContentProvider(ArrayContentProvider.getInstance());
		billingPositionsViewer.setLabelProvider(new BillingPositionLabelProvider());

		// Initialize with sample data (to be replaced with database query later)
		List<String> samplePositions = new ArrayList<>();
		samplePositions.add("00.0010 - Consultation, first 5 min");
		samplePositions.add("00.0020 - Consultation, each additional 5 min");
		samplePositions.add("00.0030 - Consultation by phone");
		samplePositions.add("00.0040 - Emergency consultation");
		samplePositions.add("00.0050 - Visit at patient's home");
		
		billingPositionsViewer.setInput(samplePositions);
	}

	/**
	 * Gets the table viewer for the billing positions.
	 * This can be used to set it as a selection provider.
	 * 
	 * @return the table viewer
	 */
	public TableViewer getBillingPositionsViewer() {
		return billingPositionsViewer;
	}

	/**
	 * Sets the input for the billing positions viewer.
	 * 
	 * @param input the input object (typically a List)
	 */
	public void setInput(Object input) {
		if (billingPositionsViewer != null && !billingPositionsViewer.getTable().isDisposed()) {
			billingPositionsViewer.setInput(input);
		}
	}

	/**
	 * Refreshes the viewer.
	 */
	public void refresh() {
		if (billingPositionsViewer != null && !billingPositionsViewer.getTable().isDisposed()) {
			billingPositionsViewer.refresh();
		}
	}
}
