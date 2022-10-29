package ch.elexis.ungrad.forms.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.pdf.Manager;
import ch.elexis.ungrad.textplugin.preferences.PreferenceConstants;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

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

	public void createPDF(Template tmpl, String printer) throws Exception {
		/*
		Manager pdf = new Manager();
			String doctype = tmpl.doctype;
			if (!StringTool.isNothing(doctype)) {
				text = text.replace("Doctype", doctype);
				text = text.replace("Doctitle", doctype);
			}
	
		String filename = new TimeTool().toString(TimeTool.FULL_ISO);
		String prefix = (new TimeTool(prefilled.get("[Datum.heute]"))).toString(TimeTool.DATE_ISO) + "_";
		if (prefilled.containsKey("[Adressat.Name]")) {
			filename = prefix + prefilled.get("[Adressat.Name]") + "_" + prefilled.get("[Adressat.Vorname]");
		} else {
			String name = "Ausgang_";
			Pattern pat = Pattern.compile("<title>(.+)</title>");
			Matcher m = pat.matcher(text);
			if (m.find()) {
				String fn = m.group(1);
				name = fn;
			}
			filename = prefix + name;
		}
		String dirname = prefilled.get("[Patient.Name]") + "_" + prefilled.get("[Patient.Vorname]") + "_"
				+ prefilled.get("[Patient.Geburtsdatum]");

		StringBuilder sb = new StringBuilder();
		sb.append(CoreHub.localCfg.get(PreferenceConstants.DOCUMENT_BASE, "")).append(File.separator)
				.append(dirname.substring(0, 1)).append(File.separator).append(dirname);

		File dir = new File(sb.toString());
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("Could not create directory " + dir.getAbsolutePath());
			}
		}
		for (Entry<String, String> e : prefilled.entrySet()) {
			text = text.replace(e.getKey(), e.getValue());
		}
		for (Entry<String, Object> e : postfilled.entrySet()) {
			text = text.replace(e.getKey(), getPostfilledFieldValue(e.getKey()));
		}
		File htmlFile = new File(dir, filename + ".html");
		FileTool.writeTextFile(htmlFile, text);
		File pdfFile = new File(dir, filename + ".pdf");
		pdf.createPDF(htmlFile, pdfFile);
		outputFile = pdfFile.getAbsolutePath();
		return outputFile;
*/
	}

	public String convertPug(String pug, String dir) throws Exception {
		dir += File.separator + "x";
		String pugbin = CoreHub.localCfg.get(PreferenceConstants.PUG, "pug");

		Process process = new ProcessBuilder(pugbin, "-p", dir).start();
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
