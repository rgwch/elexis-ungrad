package ch.elexis.ungrad;

import java.io.File;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.TimeTool;

public class StorageController {

	public File createFile(Patient pat, String title) throws Exception {
		File dir = getOutputDirFor(pat, true);
		String name = createOutputFilename(title);
		return new File(dir, name);
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
			if(p==null) {
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
