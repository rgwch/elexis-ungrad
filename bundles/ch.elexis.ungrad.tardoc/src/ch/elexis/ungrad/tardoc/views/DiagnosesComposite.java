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
 *********************************************************************************/

package ch.elexis.ungrad.tardoc.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Optional;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.ui.views.DiagnosenDisplay;
import ch.elexis.ungrad.tardoc.services.DiagnosesManager;

/**
 * Composite for searching and displaying diagnoses from ICD-10 and TI-Code systems.
 */
public class DiagnosesComposite extends Composite {

	private TableViewer diagnosesViewer;
	private Text searchField;
	private Combo codeSystemCombo;
	private DiagnosesManager diagnosesManager;
	// rivate Label statusLabel;
	private IWorkbenchPage page;
	private IEncounter currentEncounter;
	private DiagnosenDisplay diags;
	private IMemento memento;
	
	// Code system options
	private static final String[] CODE_SYSTEMS = { "Alle", "ICD-10", "TI-Code" };
	private static final String MEMENTO_CODE_SYSTEM = "selected_code_system";

	/**
	 * Event handler to refresh the billed display when encounter is updated (e.g.,
	 * after dropping items)
	 */
	@Optional
	@jakarta.inject.Inject
	public void updateEncounter(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IEncounter encounter) {
		if (encounter != null && encounter.equals(currentEncounter) && diags != null) {
			// Refresh the billed display to show newly added items
			diags.setEncounter(encounter);
		}
	}
	
	void setKons(IEncounter k) {
		currentEncounter = k;
		diags.setEncounter(k);
	}


	/**
	 * Label provider for diagnoses (IDiagnosis)
	 */
	class DiagnosisLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			if (obj instanceof IDiagnosis) {
				IDiagnosis diagnosis = (IDiagnosis) obj;
				String codeSystem = diagnosis.getCodeSystemName();
				return "[" + codeSystem + "] " + diagnosis.getCode() + " - " + diagnosis.getText();
			}
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

	/**
	 * Creates a new diagnoses composite.
	 * 
	 * @param page the workbench page
	 * @param parent the parent composite
	 * @param style  the SWT style bits
	 * @param memento the memento for restoring state (can be null)
	 */
	public DiagnosesComposite(IWorkbenchPage page, Composite parent, int style, IMemento memento) {
		super(parent, style);
		this.page = page;
		this.memento = memento;
		
		// Initialize DiagnosesManager
		diagnosesManager = new DiagnosesManager();

		createContent();
		
		// Restore saved code system selection
		restoreState();
	}

	/**
	 * Creates the content of this composite
	 */
	private void createContent() {
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		diags=new DiagnosenDisplay(page, this, SWT.NONE);
		GridData ddLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		ddLayoutData.heightHint = 150; // Minimum height of 150 pixels
		diags.setLayoutData(ddLayoutData);
		// Create search field with code system selector
		createSearchControls(this);

		
		// Create table viewer
		diagnosesViewer = new TableViewer(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		diagnosesViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		diagnosesViewer.getTable().setHeaderVisible(false);
		diagnosesViewer.getTable().setLinesVisible(true);

		// Set content and label provider
		diagnosesViewer.setContentProvider(ArrayContentProvider.getInstance());
		diagnosesViewer.setLabelProvider(new DiagnosisLabelProvider());

		// Add drag support to enable dragging items
		addDragSupport();
		
		// Add double-click listener to add diagnosis to encounter
		addDoubleClickListener();

		// Initialize with empty list
		diagnosesViewer.setInput(Collections.emptyList());
	}

	/**
	 * Creates the search controls (code system combo and search field)
	 */
	private void createSearchControls(Composite parent) {
		Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayout(new GridLayout(4, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		// Code system label
		Label codeSystemLabel = new Label(searchComposite, SWT.NONE);
		codeSystemLabel.setText("Code System:");
		codeSystemLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// Code system combo
		codeSystemCombo = new Combo(searchComposite, SWT.READ_ONLY | SWT.BORDER);
		codeSystemCombo.setItems(CODE_SYSTEMS);
		codeSystemCombo.select(0); // Default to "All"
		codeSystemCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		codeSystemCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performSearch();
			}
		});

		// Search label
		Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText("Suche:");
		searchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// Search field
		searchField = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		searchField.setMessage("Diagnosecode oder Text eingeben..."); // Placeholder text

		// Add modify listener to perform search as user types
		searchField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				performSearch();
			}
		});
	}

	/**
	 * Performs the search using DiagnosesManager
	 */
	private void performSearch() {
		String searchText = searchField.getText().trim();

		if (searchText.isEmpty()) {
			// Clear the list if search is empty
			diagnosesViewer.setInput(Collections.emptyList());
			return;
		}

		// Get selected code system
		String codeSystem = codeSystemCombo.getText();

		try {
			// Perform the search
			List<IDiagnosis> results = diagnosesManager.findDiagnoses(codeSystem, searchText);

			// Update the viewer
			diagnosesViewer.setInput(results);

		} catch (Exception ex) {
			diagnosesViewer.setInput(Collections.emptyList());
			ex.printStackTrace();
		}
	}

	/**
	 * Adds drag support to the diagnoses viewer to enable dragging items
	 */
	private void addDragSupport() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] { org.eclipse.swt.dnd.TextTransfer.getInstance() };

		DragSource dragSource = new DragSource(diagnosesViewer.getTable(), operations);
		dragSource.setTransfer(transferTypes);
		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				// Get the selected item
				org.eclipse.jface.viewers.IStructuredSelection selection = (org.eclipse.jface.viewers.IStructuredSelection) diagnosesViewer
						.getSelection();

				if (selection.isEmpty()) {
					event.data = null;
					return;
				}

				// Get the IDiagnosis object
				Object firstElement = selection.getFirstElement();
				if (firstElement instanceof IDiagnosis) {
					IDiagnosis diagnosis = (IDiagnosis) firstElement;
					// Convert to string using StoreToStringService
					String storeToString = StoreToStringServiceHolder.getStoreToString(diagnosis);
					event.data = storeToString;
				} else {
					event.data = null;
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				// Check if there is a selection
				org.eclipse.jface.viewers.IStructuredSelection selection = (org.eclipse.jface.viewers.IStructuredSelection) diagnosesViewer
						.getSelection();
				event.doit = !selection.isEmpty();
			}
		});
	}

	/**
	 * Adds double-click listener to add diagnosis to current encounter
	 */
	private void addDoubleClickListener() {
		diagnosesViewer.addDoubleClickListener(event -> {
			org.eclipse.jface.viewers.IStructuredSelection selection = 
				(org.eclipse.jface.viewers.IStructuredSelection) event.getSelection();
			
			if (selection.isEmpty() || currentEncounter == null) {
				return;
			}
			
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IDiagnosis) {
				IDiagnosis diagnosis = (IDiagnosis) firstElement;
				currentEncounter.addDiagnosis(diagnosis);
				
				// Refresh the diagnoses display to show the newly added diagnosis
				if (diags != null) {
					diags.setEncounter(currentEncounter);
				}
			}
		});
	}

	/**
	 * Gets the table viewer for the diagnoses. This can be used to set it
	 * as a selection provider.
	 * 
	 * @return the table viewer
	 */
	public TableViewer getDiagnosesViewer() {
		return diagnosesViewer;
	}

	/**
	 * Sets the input for the diagnoses viewer.
	 * 
	 * @param input the input object (typically a List)
	 */
	public void setInput(Object input) {
		if (diagnosesViewer != null && !diagnosesViewer.getTable().isDisposed()) {
			diagnosesViewer.setInput(input);
		}
	}

	/**
	 * Refreshes the viewer.
	 */
	public void refresh() {
		if (diagnosesViewer != null && !diagnosesViewer.getTable().isDisposed()) {
			diagnosesViewer.refresh();
		}
	}
	
	/**
	 * Saves the current state to the memento.
	 * 
	 * @param memento the memento to save to
	 */
	public void saveState(IMemento memento) {
		if (memento != null && codeSystemCombo != null && !codeSystemCombo.isDisposed()) {
			memento.putString(MEMENTO_CODE_SYSTEM, codeSystemCombo.getText());
		}
	}
	
	/**
	 * Restores the saved state from the memento.
	 */
	private void restoreState() {
		if (memento != null && codeSystemCombo != null && !codeSystemCombo.isDisposed()) {
			String savedCodeSystem = memento.getString(MEMENTO_CODE_SYSTEM);
			if (savedCodeSystem != null) {
				// Find the index of the saved code system
				for (int i = 0; i < CODE_SYSTEMS.length; i++) {
					if (CODE_SYSTEMS[i].equals(savedCodeSystem)) {
						codeSystemCombo.select(i);
						break;
					}
				}
			}
		}
	}
}

