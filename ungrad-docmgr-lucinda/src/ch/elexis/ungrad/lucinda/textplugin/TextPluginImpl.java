package ch.elexis.ungrad.lucinda.textplugin;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.ui.text.ITextPlugin;


public class TextPluginImpl implements ITextPlugin {

	private Composite comp;
	private Label filename_label;
	private List openFiles;
	private boolean bSaveOnFocusLost=false;
	private PageFormat format;
	

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
		this.format=f;
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
			openFiles=new List(comp, SWT.SINGLE);
			/*
			RowData data = new RowData();
			filename_label = new Label(comp, SWT.PUSH);
			filename_label.setText(NoFileOpen);
			filename_label.setLayoutData(data);
			data.width = 400;
			//open_button = new Button(comp, SWT.PUSH);
			//open_button.setText("Editor öffnen");
			open_button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event){
					openEditor();
				}
			});
			data = new RowData();
			open_button.setLayoutData(data);
			import_button = new Button(comp, SWT.PUSH);
			import_button.setText("Datei importieren");
			import_button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event){
					importFile();
				}
			});
			import_button.setLayoutData(data);
			
			comp.pack();

			Composite exporters = new Composite(parent, SWT.NONE);
			exporters.setLayout(new GridLayout());
			Exporter[] exps = Export.getExporters();
			for (Exporter e: exps) {
				Button b = new Button(exporters, SWT.PUSH);
				b.setText(e.getLabel());
				b.setData(e);
				b.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button b = (Button) e.widget;
						Exporter ex = (Exporter) b.getData();
						File f = exportPDF();
						if (f != null) ex.export(f.getPath());
					}
				});
			}
			exporters.update();
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object insertText(Object pos, String text, int adjust) {
		// TODO Auto-generated method stub
		return null;
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
