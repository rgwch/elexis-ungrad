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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.*;

import java.awt.Desktop;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.ungrad.forms.Activator;
import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Handle forms
 * 
 * @author gerry
 *
 */
public class Controller extends TableLabelProvider implements IStructuredContentProvider {

	/* CoontentProvider */
	@Override
	public Object[] getElements(Object inputElement) {
		Patient pat = (Patient) inputElement;
		File dir;
		try {
			dir = getOutputDirFor(pat, true);
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
		} catch (Exception e) {
			ExHandler.handle(e);
			return new String[] { "Error reading directory" };
		}

	}

	/* LabelProvider */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		return (String) element;
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

	/**
	 * Find the ELexis "Brief" that corresponds with a given Forms Document
	 * 
	 * @param item    Name of the douckent, as shown in the forms view
	 * @param patient Patient concerned
	 * @return The Brief or null if no such Brief was found.
	 */
	public Brief getCorrespondingBrief(String item, Person patient) {
		Pattern pat = Pattern.compile("A_([0-9]{4,4}-[0-1][0-9]-[0-3][0-9])_(.+)");
		Matcher m = pat.matcher(item);
		if (m.matches()) {
			String date = m.group(1).replace("-", "");
			String title = m.group(2);
			Query<Brief> qbe = new Query<Brief>(Brief.class);
			qbe.add(Brief.FLD_SUBJECT, Query.EQUALS, title);
			qbe.add(Brief.FLD_DATE, Query.EQUALS, date);
			qbe.add(Brief.FLD_PATIENT_ID, Query.EQUALS, patient.getId());
			List<Brief> briefe = qbe.execute();
			if (briefe.size() > 0) {
				return briefe.get(0);
			}
		}
		return null;
	}

	/**
	 * Write an HTML file from the current state of the Template. This is called
	 * with every deactivation of the Forms View to save current work if
	 * interrupted. And it's called befor pdf outputting.
	 * 
	 * @param tmpl The Template to save
	 * @return The HTML file just written
	 * @throws Exception
	 */
	public File writeHTML(Template tmpl) throws Exception {
		String filename = tmpl.getFilename();
		String prefix = "";
		File htmlFile;
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
			prefix = "A_" + new TimeTool().toString(TimeTool.DATE_ISO) + "_";
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

			File dir = getOutputDirFor(pat, true);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					throw new Exception("Could not create directory " + dir.getAbsolutePath());
				}
			}
			htmlFile = new File(dir, filename + ".html");
		} else {
			htmlFile = new File(filename);
		}
		String content = tmpl.getXml();
		FileTool.writeTextFile(htmlFile, content);
		return htmlFile;
	}

	/**
	 * Create a PDF file from a template. Connect an Elexis "Brief" to the resulting
	 * file, If there is already a Brief connected, don't create a new one but
	 * update the existing (i.e. if the user just modified and re-printed an
	 * existing document.
	 * 
	 * @param htmlFile The HTML file
	 * @param tmpl     The Template to use
	 * @return the full path of the pdf written
	 * @throws Exception
	 */
	public String createPDF(File htmlFile, Template template) throws Exception {

		Manager pdf = new Manager();
		File pdfFile = new File(htmlFile.getParent(), FileTool.getNakedFilename(htmlFile.getName()) + ".pdf");
		pdf.createPDF(htmlFile, pdfFile);
		String outputFile = pdfFile.getAbsolutePath();
		String brief = template.getBrief();
		if (brief == null) {
			Brief meta = createLinksWithElexis(outputFile, template.adressat);
			template.setBrief(meta);
		} else {
			Brief meta = Brief.load(brief);
			if (meta.isValid()) {
				meta.save(FileTool.readFile(pdfFile), "pdf");
			}

		}
		return outputFile;
	}

	/**
	 * Show an existing PDF file by launching the configured system PDF-viewer. If
	 * there is a corresponding *Brief" and the PDF's last modification is newer
	 * than the Briefs's last update, then the Brief is updated with the contents of
	 * the PDF.
	 * 
	 * @param pat   patient whose outboud directory should be searched. Can be null
	 *              -> current patient
	 * 
	 * @param title Title of the document to retrieve (as shown in the forms view)
	 * @return The full filepath of the displayed file or null
	 * @throws Exception
	 */
	public String showPDF(Patient pat, String title) throws Exception {
		if (pat == null) {
			pat = ElexisEventDispatcher.getSelectedPatient();
		}
		File dir = getOutputDirFor(pat, false);
		if (dir.exists()) {
			File outfile = new File(dir, title + ".pdf");
			if (outfile.exists()) {
				Brief brief = getCorrespondingBrief(title, pat);
				if (brief != null && brief.exists()) {
					long lBrief = brief.getLastUpdate();
					long lPdf = outfile.lastModified();
					if (lPdf - lBrief > 1000) {
						brief.save(FileTool.readFile(outfile), "pdf");
					}
				}
				String filepath = outfile.getAbsolutePath();
				String viewer = CoreHub.localCfg.get(PreferenceConstants.PDF_VIEWER, "");
				if (!StringTool.isNothing(viewer)) {
					asyncRunViewer(viewer, outfile, brief);
				} else {
					Program.launch(filepath);
				}
				return filepath;
			}
		}
		return null;
	}

	public Brief createLinksWithElexis(String filepath, Kontakt adressat) throws Exception {
		String briefTitle = FileTool.getNakedFilename(filepath);
		if (briefTitle.matches("A_[0-9]{4,4}-[0-1][0-9]-[0-3][0-9]_.+")) {
			briefTitle = briefTitle.substring(13);
		}
		Konsultation current = (Konsultation) ElexisEventDispatcher.getInstance().getSelected(Konsultation.class);
		Brief metadata = new Brief(briefTitle, new TimeTool(), CoreHub.actUser, adressat, current, "Formular");
		metadata.save(FileTool.readFile(new File(filepath)), "pdf");
		addFormToKons(metadata, current);
		return metadata;
	}

	private void addFormToKons(final Brief brief, final Konsultation kons) {
		if (kons != null) {
			if (CoreHub.getLocalLockService().acquireLock(kons).isOk()) {
				String label = "[ " + brief.getLabel().replace("_", " ") + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
				// kons.addXRef(XRefExtensionConstants.providerID, brief.getId(), -1, label);
				kons.addXRef(Activator.KonsXRef, brief.getId(), -1, label);
				CoreHub.getLocalLockService().releaseLock(kons);
			}
		}
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

	/**
	 * Revove documents from output dir (not from database)
	 * 
	 * @param item
	 * @param pat
	 * @throws Exception
	 */
	public void delete(String item, Person pat) throws Exception {
		File dir = getOutputDirFor(pat, false);
		if (dir.exists()) {
			File htmlFile = new File(dir, item + ".html");
			File pdfFile = new File(dir, item + ".pdf");
			if (pdfFile.exists()) {
				pdfFile.delete();
			}
			if (htmlFile.exists()) {
				htmlFile.delete();
			}
		}
		Brief brief = getCorrespondingBrief(item, pat);
		if (brief != null && brief.exists()) {
			Konsultation k = Konsultation.load(brief.get(Brief.FLD_KONSULTATION_ID));
			if (k.exists()) {
				k.removeXRef(Activator.KonsXRef, brief.getId());
			}
			brief.delete();
		}
	}

	public void delete(Brief brief) throws Exception {
		TimeTool tt = new TimeTool(brief.getDatum());
		String basename = "A_" + tt.toString(TimeTool.DATE_ISO) + "_" + brief.getBetreff();
		delete(basename, brief.getPatient());
	}

	void asyncRunViewer(String viewer, File pdf, Brief brief) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					long ftime = pdf.lastModified();
					Process process = Runtime.getRuntime().exec(new String[] { viewer, pdf.getAbsolutePath() });
					process.waitFor();
					int val = process.exitValue();
					if (val == 0 && brief != null) {
						long atime = pdf.lastModified();
						if ((atime - ftime) > 100) {
							brief.save(FileTool.readFile(pdf), "pdf");
						}
					}

				} catch (Exception ex) {
					ExHandler.handle(ex);
					SWTHelper.showError("Could not create or show file", ex.getMessage());
				}
			}

		});
	}
}
