package ch.elexis.ungrad.lucinda.textplugin;

import java.io.File;

import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OdtHandler {
	private OdfTextDocument odt;
	private TextPluginImpl text;
	private static Logger logger = LoggerFactory.getLogger(TextPluginImpl.pluginID);
	private File file;
	private Process editor_process;
	
	public OdtHandler(TextPluginImpl text) {
		this.text=text;
	}
	private void closeFile(){
		// System.out.println("closeFile()");
		logger.info("closeFile: " + file.toString());
		odtSync();
		file.delete();
		file = null;
	}
	
	public boolean editorRunning(){
		if (editor_process == null)
			return false;
		try {
			int exitValue = editor_process.exitValue();
			return false;	
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}
	private synchronized void odtSync(){
		if (file == null || odt == null || editorRunning()) {
			return;
		}
		
		try {
			odt.save(file);
			logger.info("odtSync: completed " + file.length() + " saved");
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}
}
