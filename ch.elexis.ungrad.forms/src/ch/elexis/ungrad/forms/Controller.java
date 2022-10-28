package ch.elexis.ungrad.forms;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Resolver;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;

public class Controller extends TableLabelProvider implements IStructuredContentProvider {
	private Patient currentPatient;

	void changePatient(Patient pat) {
		currentPatient = pat;
	}

	File getOutputDirFor(Patient p) {
		String name = p.getName();
		String fname = p.getVorname();
		String birthdate = p.getGeburtsdatum();
		File superdir = new File(CoreHub.localCfg.get(PreferenceConstants.OUTPUT, ""),
				name.substring(0, 1).toLowerCase());
		File dir = new File(superdir, name + "_" + fname + "_" + birthdate);
		return dir;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Patient pat = (Patient) inputElement;
		File dir = getOutputDirFor(pat);
		String[] files = dir.list();
		if (files == null) {
			return new String[0];
		} else {
			return files;
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		return (String) element;
	}

	public String createDocumentFrom(String template, Kontakt adressat) throws Exception {
		File tmpl = new File(template);
		String html = FileTool.readTextFile(tmpl);
		if (template.endsWith("pug")) {
			html = convertPug(html);
		}
		Map<String, IPersistentObject> replacer = new HashMap<>();
		replacer.put("Patient", ElexisEventDispatcher.getSelectedPatient());
		replacer.put("mandant", ElexisEventDispatcher.getSelectedMandator());
		replacer.put("fall", ElexisEventDispatcher.getSelected(Fall.class));
		if (adressat != null) {
			replacer.put("adressat", adressat);
		}
		Resolver resolver = new Resolver(replacer, true);
		String processed = resolver.resolve(html);
		return processed;
	}

	public String convertPug(String pug) throws Exception {
		String dir = CoreHub.localCfg.get(PreferenceConstants.TEMPLATES, ".") + File.separator + "x";
		Process process = new ProcessBuilder("pug", "-p", dir).start();
		InputStreamReader err = new InputStreamReader(process.getErrorStream());
		BufferedReader burr = new BufferedReader(err);
		InputStreamReader ir = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(ir);
		OutputStreamWriter ow = new OutputStreamWriter(process.getOutputStream());
		ow.write(pug);
		ow.flush();
		ow.close();
		String line;
		StringBuilder sb = new StringBuilder();
		StringBuilder serr = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		while ((line = burr.readLine()) != null) {
			serr.append(line);
		}
		String errmsg = serr.toString();
		if (StringTool.isNothing(errmsg)) {
			return sb.toString();
		} else {
			throw new Error(errmsg);
		}
	}
}
