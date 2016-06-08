package ch.elexis.ungrad.labview.controller;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.io.FileTool;
import ch.rgw.tools.TimeTool;

public class Exporter {
	private Resolver resolver = new Resolver();
	private LabContentProvider lcp;
	Logger log = Logger.getLogger(getClass().getName());

	public Exporter(LabContentProvider lcp) {
		this.lcp = lcp;
	}

	public boolean runInBrowser() {
		try {
			String output = makeHtml();
			File tmp = File.createTempFile("ungrad", ".html");
			tmp.deleteOnExit();
			FileTool.writeTextFile(tmp, output);
			Program proggie = Program.findProgram("html");
			if (proggie != null) {
				proggie.execute(tmp.getAbsolutePath());
			} else {
				if (Program.launch(tmp.getAbsolutePath()) == false) {
					Runtime.getRuntime().exec(tmp.getAbsolutePath());
				}
			}
			return true;

		} catch (Exception ex) {
			log.log(Level.SEVERE, "could not create HTML " + ex.getMessage());
			return false;
		}
	}

	public boolean createHTML(Composite parent) {
		FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);
		String file = fd.open();
		if (file != null) {
			try {
				String output = makeHtml();
				FileTool.writeTextFile(new File(file), output);
				return true;
			} catch (Exception e) {
				log.severe("Could not create HTML " + e.getMessage());
				return false;
			}
		}
		return false;
	}

	private String makeHtml() throws Exception {
		StringBuilder html = new StringBuilder("<table>");
		TimeTool[] dates = lcp.lrs.getDates();
		html.append(makeFullGrid(dates));
		html.append("</table>");
		String rawTemplate = FileTool.readTextFile(new File("/Users/gerry/elexis/laborblatt_beispiel.html"));
		String template = resolver.resolve(rawTemplate);
		String output = template.replace("[Laborwerte]", html.toString());
		return output;
	}

	private String makeFullGrid(TimeTool[] dates) {
		StringBuilder ret = new StringBuilder("<table class=\"fullgrid\">");
		ret.append("<tr><th>Parameter</th><th>Referenz</th>");
		for (int i = dates.length - 1; i > -1; i--) {
			ret.append("<th>").append(dates[i].toString(TimeTool.DATE_GER)).append("</th>");
		}
		for (Object o : lcp.getElements(this)) {
			ret.append("<tr><th>").append(o.toString()).append("</th></tr>");
			for (Object l : lcp.getChildren(o)) {
				if (l instanceof LabResultsRow) {
					LabResultsRow lr = (LabResultsRow) l;
					ret.append("<tr>");
					ret.append("<td><em>").append(lr.getItem().get("titel")).append("</em></td><td>")
							.append(lr.getItem().get("refMann")).append("</td>");
					for (int i = dates.length - 1; i > -1; i--) {
						Result res = lr.get(dates[i]);
						if(res==null){
							ret.append("<td></td>");
						}else{
							ret.append("<td>").append(res.get("resultat")).append("</td>");
						}
					}
					ret.append("</tr>");
				}
			}

		}
		return ret.toString();
	}
}
