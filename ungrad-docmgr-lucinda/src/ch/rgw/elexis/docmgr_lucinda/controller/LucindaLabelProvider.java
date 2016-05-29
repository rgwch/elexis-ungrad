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

package ch.rgw.elexis.docmgr_lucinda.controller;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.rgw.elexis.docmgr_lucinda.Preferences;
import ch.rgw.elexis.docmgr_lucinda.model.Document;
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
			return e.get("title"); //$NON-NLS-1$
		default:
			return "?"; //$NON-NLS-1$
		}
	}

}
