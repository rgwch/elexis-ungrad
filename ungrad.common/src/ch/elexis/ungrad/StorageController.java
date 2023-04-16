/*******************************************************************************
 * Copyright (c) 2018-2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad;

import java.io.File;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.io.FileTool;
import ch.rgw.tools.TimeTool;

/**
 * Manage patient related directories in docbase
 * 
 * @author gerry
 *
 */
public class StorageController {

	public File createOutputFile(Patient pat, String title) throws Exception {
		File dir = getOutputDirFor(pat, true);
		String name = createOutputFilename(title);
		File ret = new File(dir, name);
		while (ret.exists()) {
			String base = FileTool.getNakedFilename(name);
			String ext = FileTool.getExtension(name);
			base = base + "_" + new TimeTool().toString(TimeTool.TIME_COMPACT);
			ret = new File(dir, base + "." + ext);
		}
		return ret;
	}

	/**
	 * Find the configured output dir for a patient (highly opinionated filepath
	 * resolution)
	 * 
	 * @param p                  Patient whose output dir should be retrieved
	 * @param bCreateIfNotExists create directory if it doesn't exist
	 * @return The directory to store documents for that patient.
	 */
	public File getOutputDirFor(Person p, boolean bCreateIfNotExists) throws Exception {
		if (p == null) {
			p = ElexisEventDispatcher.getSelectedPatient();
			if (p == null) {
				return null;
			}
		}
		String name = p.getName();
		String fname = p.getVorname();
		String birthdate = p.getGeburtsdatum();
		File superdir = new File(CoreHub.localCfg.get(ch.elexis.ungrad.PreferenceConstants.DOCBASE, ""),
				name.substring(0, 1).toLowerCase());
		File dir = new File(superdir, name + "_" + fname + "_" + birthdate);
		if (!dir.exists() && bCreateIfNotExists) {
			if (!dir.mkdirs()) {
				throw new Exception("Can't create output dir");
			}
		}

		return dir;
	}

	public String createOutputFilename(String basename) {
		StringBuilder ret = new StringBuilder();
		ret.append("A_").append(new TimeTool().toString(TimeTool.DATE_ISO)).append("_").append(basename);
		return ret.toString();
	}

}
