package ch.elexis.ungrad.tardoc.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.util.CoverageComparator;
import ch.elexis.core.ui.views.provider.CoverageColorLabelProvider;

public class CasesComposite extends Composite {
	private TardocKonsView tkv;
	TableComboViewer tableComboViewerFall;
	private ComboFallSelectionListener comboFallSelectionListener;

	public CasesComposite(Composite parent, TardocKonsView view) {
		super(parent, SWT.NONE);
		this.tkv = view;
		tableComboViewerFall = new TableComboViewer(parent, SWT.SINGLE | SWT.BORDER);
		tableComboViewerFall.setContentProvider(ArrayContentProvider.getInstance());
		tableComboViewerFall.setLabelProvider(new CoverageColorLabelProvider());

		comboFallSelectionListener = new ComboFallSelectionListener(this.tkv);
		tableComboViewerFall.addSelectionChangedListener(comboFallSelectionListener);
		GridData gdFall = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tableComboViewerFall.getTableCombo().setLayoutData(gdFall);
		tableComboViewerFall.getTableCombo().setTableVisible(true);
		tableComboViewerFall.getTableCombo().setTableVisible(false);

	}

	void refreshCases(IEncounter actEncounter) {
		IPatient pat = ContextServiceHolder.get().getRootContext().getTyped(IPatient.class).orElse(null);
		if (pat != null && tableComboViewerFall != null) {
			List<ICoverage> coverages = pat.getCoverages();
			Collections.sort(coverages, new CoverageComparator());
			tableComboViewerFall.setInput(coverages);
			if (actEncounter != null) {
				comboFallSelectionListener.ignoreSelectionEventOnce();
				tableComboViewerFall.setSelection(new StructuredSelection(actEncounter.getCoverage()));
			}
			// needed for initial background colours
			tableComboViewerFall.getTableCombo().setTableVisible(true);
			tableComboViewerFall.getTableCombo().setTableVisible(false);
			tableComboViewerFall.refresh();
		}
	}
	
	void setEncounter(ICoverage coverage) {
		comboFallSelectionListener.ignoreSelectionEventOnce();
		tableComboViewerFall.setSelection(new StructuredSelection(coverage));
		tableComboViewerFall.getTableCombo().setEnabled(coverage.isOpen());
	}
}
