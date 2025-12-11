package ch.elexis.ungrad.tardoc.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung;
import ch.elexis.ungrad.tardoc.services.TardocManager;
import ch.elexis.ungrad.tardoc.services.TardocManagerHolder;

/**
 * Composite for displaying billing positions with a table viewer.
 * This component is extracted from TardocKonsView to allow better separation of concerns
 * and to enable show/hide functionality.
 */
public class BillingPositionsComposite extends Composite {

	private TableViewer billingPositionsViewer;
	private Group billingGroup;
	private Text searchField;
	private TardocManager tardocManager;
	private BundleContext bundleContext;
	private Label statusLabel;

	/**
	 * Label provider for billing positions (ITardocLeistung)
	 */
	class BillingPositionLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			if (obj instanceof ITardocLeistung) {
				ITardocLeistung leistung = (ITardocLeistung) obj;
				return leistung.getCode() + " - " + leistung.getText();
			}
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
		
		// Initialize TardocManager
		bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		tardocManager = TardocManagerHolder.get();
		if (tardocManager == null) {
			tardocManager = new TardocManager();
		}
		
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

		// Create search field
		createSearchField(billingGroup);

		// Create table viewer
		billingPositionsViewer = new TableViewer(billingGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		billingPositionsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		billingPositionsViewer.getTable().setHeaderVisible(true);
		billingPositionsViewer.getTable().setLinesVisible(true);

		// Set content and label provider
		billingPositionsViewer.setContentProvider(ArrayContentProvider.getInstance());
		billingPositionsViewer.setLabelProvider(new BillingPositionLabelProvider());

		// Create status label
		statusLabel = new Label(billingGroup, SWT.NONE);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		statusLabel.setText("Enter search term to find billing positions");
		
		// Initialize with empty list
		billingPositionsViewer.setInput(Collections.emptyList());
	}

	/**
	 * Creates the search field with a label
	 */
	private void createSearchField(Composite parent) {
		Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayout(new GridLayout(2, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText("Search:");
		searchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		searchField = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		searchField.setMessage("Type to search..."); // Placeholder text

		// Add modify listener to perform search as user types
		searchField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				performSearch();
			}
		});
	}

	/**
	 * Performs the search using TardocManager
	 */
	private void performSearch() {
		String searchText = searchField.getText().trim();
		
		if (searchText.isEmpty()) {
			// Clear the list if search is empty
			billingPositionsViewer.setInput(Collections.emptyList());
			statusLabel.setText("Enter search term to find billing positions");
			return;
		}

		// Check if service is available
		if (tardocManager == null || !tardocManager.isServiceAvailable(bundleContext)) {
			statusLabel.setText("Service not available");
			billingPositionsViewer.setInput(Collections.emptyList());
			return;
		}

		try {
			// Perform the search
			List<ITardocLeistung> results = tardocManager.getLeistungen(searchText, bundleContext);
			
			// Update the viewer
			billingPositionsViewer.setInput(results);
			
			// Update status
			if (results.isEmpty()) {
				statusLabel.setText("No results found for: " + searchText);
			} else {
				statusLabel.setText("Found " + results.size() + " result(s)");
			}
		} catch (Exception ex) {
			statusLabel.setText("Error searching: " + ex.getMessage());
			billingPositionsViewer.setInput(Collections.emptyList());
			ex.printStackTrace();
		}
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
