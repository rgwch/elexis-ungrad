/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.forms.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Controller extends TableLabelProvider implements IStructuredContentProvider {
	private Patient currentPatient;

	void changePatient(Patient pat) {
		currentPatient = pat;
	}

	public File getOutputDirFor(Patient p) {
		if (p == null) {
			p = ElexisEventDispatcher.getSelectedPatient();
		}
		String name = p.getName();
		String fname = p.getVorname();
		String birthdate = p.getGeburtsdatum();
		File superdir = new File(CoreHub.localCfg.get(PreferenceConstants.OUTPUT, ""),
				name.substring(0, 1).toLowerCase());
		File dir = new File(superdir, name + "_" + fname + "_" + birthdate);
		return dir;
	}

	/* CoontentProvider */
	@Override
	public Object[] getElements(Object inputElement) {
		Patient pat = (Patient) inputElement;
		File dir = getOutputDirFor(pat);
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("A_")) {
					String ext = FileTool.getExtension(name);
					return ext.equalsIgnoreCase("pdf") || ext.equalsIgnoreCase("html");
				} else {
					return false;
				}
			}
		});
		if (files == null) {
			return new String[0];
		} else {
			Set<String> deduplicated = new LinkedHashSet<String>();
			for (String file : files) {
				deduplicated.add(FileTool.getNakedFilename(file));
			}
			String[] ret = deduplicated.toArray(new String[0]);
			return ret;
		}
	}

	/* LabelProvider */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		return (String) element;
	}

	public String createPDF(Template tmpl, String printer) throws Exception {

		Manager pdf = new Manager();
		String filename = tmpl.getFilename();
		File htmlFile, pdfFile;
		if (StringTool.isNothing(filename)) {
			String doctype = tmpl.getDoctype();
			if (!StringTool.isNothing(doctype)) {
				tmpl.replace("Doctype", doctype);
			}
			String doctitle = tmpl.getTitle();
			if (!StringTool.isNothing(doctitle)) {
				tmpl.replace("Doctitle", doctitle);
			}

			filename = new TimeTool().toString(TimeTool.FULL_ISO);
			String prefix = "A_" + new TimeTool().toString(TimeTool.DATE_ISO) + "_";
			if (!StringTool.isNothing(tmpl.getTitle())) {
				prefix += tmpl.getTitle() + "_";
			}
			if (tmpl.adressat != null) {
				filename = prefix + tmpl.adressat.get(Kontakt.FLD_NAME1) + "_" + tmpl.adressat.get(Kontakt.FLD_NAME2);
			} else {
				String name = "Ausgang";
				filename = prefix + name;
			}
			Patient pat = ElexisEventDispatcher.getSelectedPatient();
			String dirname = pat.getName() + "_" + pat.getVorname() + "_" + pat.getGeburtsdatum();

			StringBuilder sb = new StringBuilder();
			sb.append(CoreHub.localCfg.get(PreferenceConstants.OUTPUT, "")).append(File.separator)
					.append(dirname.substring(0, 1).toLowerCase()).append(File.separator).append(dirname);

			File dir = new File(sb.toString());
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					throw new Exception("Could not create directory " + dir.getAbsolutePath());
				}
			}
			htmlFile = new File(dir, filename + ".html");
			pdfFile = new File(dir, filename + ".pdf");
		} else {
			htmlFile = new File(filename);
			pdfFile = new File(FileTool.getFilepath(filename), FileTool.getNakedFilename(filename) + ".pdf");
		}
		String content = tmpl.getXml();
		FileTool.writeTextFile(htmlFile, content);
		pdf.createPDF(htmlFile, pdfFile);
		String outputFile = pdfFile.getAbsolutePath();
		Konsultation current = (Konsultation) ElexisEventDispatcher.getInstance().getSelected(Konsultation.class);
		// Brief metadata = new Brief(filename, new TimeTool(), CoreHub.actUser,
		// tmpl.adressat, current, "Formular");
		// metadata.save(content);
		return outputFile;
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
