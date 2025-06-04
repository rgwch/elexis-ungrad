/*******************************************************************************
 * Copyright (c) 2023-2025, G. Weirich and Elexis
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.ungrad.Http;
import ch.elexis.ungrad.StorageController;
import ch.elexis.ungrad.lucinda.Client3;
import ch.elexis.ungrad.lucinda.Client3.INotifier;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VersionedResource;

public class Controller extends TableLabelProvider implements IStructuredContentProvider {
	private StorageController sc = new StorageController();
	@Inject
	UISynchronize sync;

	@Override
	public Object[] getElements(Object dirname) {
		File dir = new File((String) dirname);
		if (dir.exists()) {
			File[] files = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".meta")) {
						return false;
					}
					return true;
				}
			});
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

	public void moveFileToDocbase(String concerns_id, File f, String destName, boolean bUseKI) throws Exception {
		File dir = sc.getOutputDirFor(concerns_id, true);
		File dest = new File(dir, destName);
		FileTool.copyFile(f, dest, FileTool.FAIL_IF_EXISTS);
		File meta = new File(f.getAbsolutePath() + ".meta");
		if (meta.exists()) {
			meta.delete();
		}
		f.delete();
		if (bUseKI) {
			Job job = Job.create("KI Analyze", (ICoreRunnable) monitor -> {
				try {
					Patient pat = Patient.load(concerns_id);
					Http http = new Http();
					String API_TYPE = CoreHub.localCfg.get(PreferenceConstants.AI_MODEL, "ollama");
					if (API_TYPE.equals("ollama")) {
						Client3 client = new Client3();
						client.analyzeFile(FileTool.readFile(dest), new INotifier() {
							StringBuilder sb = new StringBuilder();

							@Override
							public boolean received(String text) {
								if (!StringTool.isNothing(text) && text.length() > 3) {
									sb.append(text);
								} else {
									String model = "gemma3:12b"; // TODO: Make configurable
									String prompt = "Bitte erstelle eine Zusammenfassung aus folgendem Text: "; // TODO:
																												// Make
																												// configurable
									String requestBody = String.format("{ \"model\": \"%s\", \"prompt\": \"%s\" }",
											model, prompt + sb.toString());
									try {
										URL url = new URL(CoreHub.localCfg.get(PreferenceConstants.AI_URL, ""));
										String result = http.doPost(url, requestBody, 200);
										if (!StringTool.isNothing(result)) {
											addToEintrag(pat, result);
										}
									} catch (Exception e) {
										ExHandler.handle(e);
									}
								}
								return false;
							}
						});
					} else {
						URL url = new URL(
								CoreHub.localCfg.get(PreferenceConstants.AI_URL, "") + "/" + dest.getAbsolutePath());
						byte[] result = http.doGet(url);
						addToEintrag(pat, new String(result, "utf-8"));
					}

					// });

				} catch (Exception ex) {
					SWTHelper.showError("Fehler bei KI Aufruf", ex.getMessage());
				}

			});
			job.schedule();
		}
	}

	private void addToEintrag(Patient pat, String text) throws UnsupportedEncodingException {
		Fall currentCase = pat.getLastKonsultation().getFall();
		Konsultation k = currentCase.neueKonsultation();
		VersionedResource eintrag = k.getEintrag();
		Samdas samdas = new Samdas(text);
		// sync.asyncExec(()->{
		eintrag.update(samdas.toString(), "summary added by AI");
		k.setEintrag(eintrag, false);
	}
}
