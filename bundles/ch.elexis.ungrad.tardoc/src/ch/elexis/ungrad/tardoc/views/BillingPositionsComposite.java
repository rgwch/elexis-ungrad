/*******************************************************************************
* Copyright (c) 2025 by G. Weirich
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
* Substantial contributions:  Copilot (c) 2024 GitHub, Inc. using Claude Sonnet 4.5
*********************************************************************************/

package ch.elexis.ungrad.tardoc.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Optional;

import ch.elexis.ungrad.tardoc.Messages;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import ch.elexis.base.ch.arzttarife.tardoc.ITardocLeistung;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.ui.processor.BillingProcessor;
import ch.elexis.core.ui.views.VerrechnungsDisplay;
import ch.elexis.ungrad.tardoc.services.TardocManager;

/**
 * Composite for displaying billed positions and billing positions.
 */
public class BillingPositionsComposite extends Composite {

	private TableViewer billingPositionsViewer;
	private Text searchField;
	private TardocManager tardocManager;
	private BundleContext bundleContext;
	private TardocKonsView tkv;
	private VerrechnungsDisplay billed;
	private IEncounter currentEncounter;

	/**
	 * Event handler to refresh the billed display when encounter is updated (e.g.,
	 * after dropping items)
	 */
	@Optional
	@jakarta.inject.Inject
	public void updateEncounter(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IEncounter encounter) {
		if (encounter != null && encounter.equals(currentEncounter) && billed != null) {
			// Refresh the billed display to show newly added items
			billed.setEncounter(encounter);
		}
	}

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
	 * @param style  the SWT style bits
	 */
	public BillingPositionsComposite(Composite parent, int style, TardocKonsView view) {
		super(parent, style);
		this.tkv = view;

		// Initialize TardocManager
		bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		tardocManager = TardocManager.getInstance();

		createContent();
	}

	void setKons(IEncounter k) {
		currentEncounter = k;
		billed.setEncounter(k);
	}

	/**
	 * Creates the content of this composite
	 */
	private void createContent() {
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Create billed composite with 50% height
		billed = new VerrechnungsDisplay(tkv.getSite().getPage(), this, SWT.NONE);
		GridData billedGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		billedGridData.minimumHeight = 100;
		billedGridData.heightHint = 700;
		billed.setLayoutData(billedGridData);

		// Create search field
		createSearchField(this);

		// Create table viewer with 50% height
		billingPositionsViewer = new TableViewer(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData viewerGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewerGridData.minimumHeight = 100;
		viewerGridData.heightHint = 300;
		billingPositionsViewer.getTable().setLayoutData(viewerGridData);
		billingPositionsViewer.getTable().setHeaderVisible(false);
		billingPositionsViewer.getTable().setLinesVisible(true);

		// Set content and label provider
		billingPositionsViewer.setContentProvider(ArrayContentProvider.getInstance());
		billingPositionsViewer.setLabelProvider(new BillingPositionLabelProvider());

		// Add drag support to enable dragging items to VerrechnungsDisplay
		addDragSupport();

		// Add double-click support to add items directly to the encounter
		addDoubleClickSupport();

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
		searchLabel.setText(Messages.BillingPositionsComposite_Search_Label);
		searchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		searchField = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		searchField.setMessage(Messages.BillingPositionsComposite_Search_Placeholder); // Placeholder text

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
			return;
		}

		// Check if service is available
		if (tardocManager == null || !tardocManager.isServiceAvailable(bundleContext)) {
			billingPositionsViewer.setInput(Collections.emptyList());
			return;
		}

		try {
			// Perform the search
			List<ITardocLeistung> results = tardocManager.getLeistungen(searchText, bundleContext);

			// Update the viewer
			billingPositionsViewer.setInput(results);

		} catch (Exception ex) {
			billingPositionsViewer.setInput(Collections.emptyList());
			ex.printStackTrace();
		}
	}

	/**
	 * Adds drag support to the billing positions viewer to enable dragging items to
	 * VerrechnungsDisplay
	 */
	private void addDragSupport() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] { org.eclipse.swt.dnd.TextTransfer.getInstance() };

		DragSource dragSource = new DragSource(billingPositionsViewer.getTable(), operations);
		dragSource.setTransfer(transferTypes);
		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Get the selected item
				org.eclipse.jface.viewers.IStructuredSelection selection = (org.eclipse.jface.viewers.IStructuredSelection) billingPositionsViewer
						.getSelection();

				if (selection.isEmpty()) {
					event.data = null;
					return;
				}

				// Get the ITardocLeistung object
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof ITardocLeistung) {
					ITardocLeistung leistung = (ITardocLeistung) firstElement;
					// Convert to string using StoreToStringService
					String storeToString = StoreToStringServiceHolder.getStoreToString(leistung);
					event.data = storeToString;
				} else {
					event.data = null;
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				// Check if there is a selection
				org.eclipse.jface.viewers.IStructuredSelection selection = (org.eclipse.jface.viewers.IStructuredSelection) billingPositionsViewer
						.getSelection();
				event.doit = !selection.isEmpty();
			}
		});
	}

	/**
	 * Gets the table viewer for the billing positions. This can be used to set it
	 * as a selection provider.
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

	/**
	 * Adds double-click support to the billing positions viewer to add items
	 * directly to the encounter
	 */
	private void addDoubleClickSupport() {
		billingPositionsViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// Get the selected item
				org.eclipse.jface.viewers.IStructuredSelection selection = (org.eclipse.jface.viewers.IStructuredSelection) billingPositionsViewer
						.getSelection();

				if (selection.isEmpty() || currentEncounter == null) {
					return;
				}

				// Get the ITardocLeistung object
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof ITardocLeistung) {
					ITardocLeistung leistung = (ITardocLeistung) firstElement;

					// Add the leistung to the encounter using BillingService
					try {
						BillingProcessor bp = new BillingProcessor(currentEncounter);
						bp.processOtherObject(leistung);
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, currentEncounter);

						/*
						 * ch.elexis.core.services.holder.BillingServiceHolder.get() .bill(leistung,
						 * currentEncounter, 1.0); // Trigger update event to refresh the display
						 */
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	}
}
