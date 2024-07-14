/*******************************************************************************
 * Copyright (c) 2018 by G. Weirich
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

package ch.elexis.ungrad.labenter.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.data.LabItem;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		setDescription("Laboritems für manuelle Eingabe");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	public void createFieldEditors() {

		addField(new ItemsEditor(PreferenceConstants.P_ITEMS, "labItems", getFieldEditorParent()));
		/*
		 * addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH,
		 * "&Directory preference:", getFieldEditorParent())); addField( new
		 * BooleanFieldEditor( PreferenceConstants.P_BOOLEAN,
		 * "&An example of a boolean preference", getFieldEditorParent()));
		 * 
		 * addField(new RadioGroupFieldEditor( PreferenceConstants.P_CHOICE,
		 * "An example of a multiple-choice preference", 1, new String[][] { {
		 * "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } },
		 * getFieldEditorParent())); addField( new
		 * StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:",
		 * getFieldEditorParent()));
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	class ItemsEditor extends ListEditor {
		ArrayList<LabItem> items;
		Map<String, String> map;

		public ItemsEditor(String name, String label, org.eclipse.swt.widgets.Composite parent) {
			super(name, label, parent);
		}

		@Override
		protected String createList(String[] labels) {
			StringBuilder ret = new StringBuilder();
			for (String label : labels) {
				ret.append(map.get(label)).append(",");
			}
			return ret.deleteCharAt(ret.length() - 1).toString();

		}

		@Override
		protected String getNewInputObject() {
			LabItemSelector lis = new LabItemSelector(getShell());
			if (lis.open() == Dialog.OK) {
				LabItem it = lis.result;
				map.put(it.getLabel(), it.getId());
				return it.getLabel();
			}
			return null;
		}

		@Override
		protected String[] parseString(String in) {
			items = new ArrayList<LabItem>();
			ArrayList<String> ret = new ArrayList<String>();
			map = new HashMap<>();
			for (String item : in.split(",")) {
				if (!item.isEmpty()) {
					LabItem labItem = LabItem.load(item);
					if (labItem.isValid()) {
						items.add(labItem);
						ret.add(labItem.getLabel());
						map.put(labItem.getLabel(), labItem.getId());
					}
				}
			}
			return ret.toArray(new String[0]);
		}

	}

	@Override
	public boolean performOk() {
		((SettingsPreferenceStore) getPreferenceStore()).flush();
		return super.performOk();
	}

}