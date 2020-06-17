package ch.elexis.ungrad.lucinda.textplugin;

import org.odftoolkit.odfdom.doc.OdfTextDocument;

public class OdtHandler {
	private OdfTextDocument odt;
	private TextPluginImpl text;
	
	public OdtHandler(TextPluginImpl text) {
		this.text=text
	}
	private void closeFile(){
		// System.out.println("closeFile()");
		logger.info("closeFile: " + file.toString());
		odtSync();
		file.delete();
		file = null;
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
