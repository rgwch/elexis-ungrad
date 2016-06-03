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

package ch.elexis.ungrad.lucinda.controller;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.model.Document;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class LucindaLabelProvider extends TableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		Document e = new Document(element);
		switch (columnIndex) {
		case 0:
			return e.get(Preferences.FLD_LUCINDA_DOCTYPE);
		case 1:
			return e.get("lastname"); //$NON-NLS-1$
		case 2:
			return e.getDate().toString(TimeTool.DATE_MYSQL);
		case 3:
			String cat = e.get("category");//$NON-NLS-1$
			if (StringTool.isNothing(cat)) {
				cat = e.get("Kategorie");//$NON-NLS-1$
			}
			if (StringTool.isNothing(cat)) {
				return e.get("title"); //$NON-NLS-1$
			} else {
				return cat + ": " + e.get("title"); //$NON-NLS-1$
			}
		default:
			return "?"; //$NON-NLS-1$
		}
	}

}
