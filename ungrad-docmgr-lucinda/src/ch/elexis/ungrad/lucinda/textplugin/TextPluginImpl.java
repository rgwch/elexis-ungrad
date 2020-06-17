package ch.elexis.ungrad.lucinda.textplugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.OdfXMLFactory;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;

public class TextPluginImpl implements ITextPlugin {

	private static final String pluginID = "ch.elexis.ungrad.docmgr-lucinda";
	private Composite comp;
	private Label filename_label;
	private File file;
	private List openFiles;
	private boolean bSaveOnFocusLost = false;
	private PageFormat format;
	
	private static Logger logger = LoggerFactory.getLogger(pluginID);
	private Process editor_process;
	private OdtHandler odt=new OdtHandler(this);
	
	@Override
	public void setInitializationData(IConfigurationElement arg0, String arg1, Object arg2) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public PageFormat getFormat() {
		return this.format;
	}

	@Override
	public void setFormat(PageFormat f) {
		this.format = f;
	}

	@Override
	public void setParameter(Parameter parameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public Composite createContainer(Composite parent, ICallback handler) {

		if (comp == null) {
			comp = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout(SWT.VERTICAL);
			layout.wrap = true;
			layout.fill = false;
			layout.justify = false;
			comp.setLayout(layout);
			openFiles = new List(comp, SWT.SINGLE);
			/*
			 * RowData data = new RowData(); filename_label = new Label(comp, SWT.PUSH);
			 * filename_label.setText(NoFileOpen); filename_label.setLayoutData(data);
			 * data.width = 400; //open_button = new Button(comp, SWT.PUSH);
			 * //open_button.setText("Editor öffnen");
			 * open_button.addListener(SWT.Selection, new Listener() { public void
			 * handleEvent(Event event){ openEditor(); } }); data = new RowData();
			 * open_button.setLayoutData(data); import_button = new Button(comp, SWT.PUSH);
			 * import_button.setText("Datei importieren");
			 * import_button.addListener(SWT.Selection, new Listener() { public void
			 * handleEvent(Event event){ importFile(); } });
			 * import_button.setLayoutData(data);
			 * 
			 * comp.pack();
			 * 
			 * Composite exporters = new Composite(parent, SWT.NONE);
			 * exporters.setLayout(new GridLayout()); Exporter[] exps =
			 * Export.getExporters(); for (Exporter e: exps) { Button b = new
			 * Button(exporters, SWT.PUSH); b.setText(e.getLabel()); b.setData(e);
			 * b.addSelectionListener(new SelectionAdapter() {
			 * 
			 * @Override public void widgetSelected(SelectionEvent e) { Button b = (Button)
			 * e.widget; Exporter ex = (Exporter) b.getData(); File f = exportPDF(); if (f
			 * != null) ex.export(f.getPath()); } }); } exporters.update();
			 */
		}

		return comp;

	}

	@Override
	public void setFocus() {
		this.comp.setFocus();

	}

	@Override
	public void dispose() {
		this.comp.dispose();

	}

	@Override
	public void showMenu(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showToolbar(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSaveOnFocusLost(boolean bSave) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean createEmptyDocument() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		logger.info("loadFromByteArray: asTemplate " + asTemplate);
		ByteArrayInputStream stream = new ByteArrayInputStream(bs);
		return loadFromStream(stream, asTemplate);
	}

	private boolean editorRunning(){
		if (editor_process == null)
			return false;
		try {
			int exitValue = editor_process.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

	/**
	 * Sicherstellen dass kein Editor geoeffnet ist. Falls einer geoeffnet ist, wird eine
	 * Fehlermeldung mit einem entsprechenden Hinweis angezeigt.
	 * 
	 * @return True wenn keine Instanz mehr geoeffnet ist.
	 */
	private boolean ensureClosed(){
		Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
		if (actPatient != null) {
			logger.info("ensureClosed: " + actPatient.getVorname() + " "
				+ actPatient.getName().toString());
		}
		
		while (editorRunning()) {
			logger.info("Editor already opened file " + file.getAbsolutePath());
			SWTHelper
				.showError(
					"Editor bereits geöffnet",
					"Es scheint bereits ein Editor geöffnet zu sein für "
						+ file.getAbsolutePath()
						+ " geöffnet zu sein.\n\n"
						+ "Falls Sie sicher sind, dass kein Editor diese Datei mehr offen hat, müssen Sie Elexis neu starten.\n\n"
						+ "Falls Sie diese Warnung nicht beachten werden die in der Datei gemachten Änderungen nicht in der Elexis Datenbank gespeichert!");
			return false;
		}
		return true;
	}
	
	
	
	
	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		logger.info("loadFromStream: " + (file != null));
		if (!ensureClosed()) {
			return false;
		}
		
		if (file != null) {
			closeFile();
		}
		
		try {
			file = File.createTempFile(getTempPrefix(), ".odt");
			logger.info("loadFromStream: " + file.toString());
			file.deleteOnExit();
			
			odt = (OdfTextDocument) OdfDocument.loadDocument(is);
			odt.save(file);
			fileValid();
			logger.info("loadFromStream: saved (but not yet converted) " + file.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("loadFromStream: loading document failed ");
			return false;
		}
		
		return true;
	}

	@Override
	public boolean findOrReplace(String pattern, ReplaceCallback cb) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] storeToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean insertTable(String place, int properties, String[][] contents, int[] columnSizes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setFont(String name, int style, float size) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setStyle(int style) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object insertText(String marke, String text, int adjust) {
		if (!ensureClosed() || file == null) {
			return null;
		}
		
		// System.out.println("insertText('" + marke + "', '" + text + "')");
		try {
			OdfFileDom contentDom = odt.getContentDom();
			XPath xpath = odt.getXPath();
			
			java.util.List<Text> texts =
				findTextNode(contentDom, xpath, Pattern.compile(Pattern.quote(marke)), true);
			
			if (texts.size() == 0) {
				return null;
			}
			
			Text txt = texts.get(0);
			txt.setTextContent(text);
			txt = formatText(contentDom, txt);
			
			// TODO: Style
			odtSync();
			return txt;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object insertText(Object pos, String text, int adjust) {
		if (!ensureClosed() || file == null || pos == null) {
			return null;
		}
		
		// System.out.println("insertText2('" + text + "')");
		try {
			OdfFileDom contentDom = odt.getContentDom();
			Text prev = (Text) pos;
			
			curStyle.setAlign(adjust);
			
			TextSpanElement span =
				(TextSpanElement) OdfXMLFactory.newOdfElement(contentDom,
					TextSpanElement.ELEMENT_NAME);
			span.setTextContent(text);
			span.setStyleName(curStyle.getTextLbl());
			
			int i;
			Text txt = prev;
			for (i = 0; i < span.getChildNodes().getLength(); i++) {
				Node n = span.getChildNodes().item(i);
				if (n instanceof Text) {
					txt = (Text) n;
					formatText(contentDom, txt);
				}
			}
			prev.getParentNode().insertBefore(span, prev.getNextSibling());
			curStyle.clearAlign();
			return txt;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean clear() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDirectOutput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initTemplatePrintSettings(String template) {
		// TODO Auto-generated method stub

	}

}
