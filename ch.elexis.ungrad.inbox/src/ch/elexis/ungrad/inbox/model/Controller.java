/*******************************************************************************
 * Copyright (c) 2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.inbox.model;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Person;
import ch.elexis.ungrad.StorageController;
import ch.rgw.io.FileTool;

public class Controller extends TableLabelProvider implements IStructuredContentProvider {
	private StorageController sc = new StorageController();

	@Override
	public Object[] getElements(Object dirname) {
		File dir = new File((String) dirname);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if (files != null) {
				return files;
			}
		}
		return new File[0];
	}

	/* LabelProvider */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		return ((File) element).getName();
	}

	public void moveFileToDocbase(Person p, File f, String destName) throws Exception {
		File dir = sc.getOutputDirFor(p, true);
		FileTool.copyFile(f, new File(dir, destName), FileTool.FAIL_IF_EXISTS);
		f.delete();
	}
}
