/**
 * Copyright (c) 2022-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 */

package ch.elexis.ungrad.text.templator.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;

<<<<<<< HEAD
=======
import ch.elexis.core.text.ITextPlugin;
>>>>>>> branch 'ungrad-2024' of ssh://pinas:/git/elexis-ungrad-plugins
import ch.elexis.core.text.MimeTypeUtil;
import ch.elexis.core.text.ReplaceCallback;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Brief;
import ch.elexis.ungrad.text.templator.model.ODFDoc;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

/**
 * The ITextPlugin. 
 * 
 * @author gerry
 *
 */
public class OdfTextPlugin implements ITextPlugin, ch.elexis.core.ui.text.ITextPlugin {
	// private Map<String, String> fields;
	private ODFDoc doc = new ODFDoc();
	private OdfTemplateFieldsDisplay display;
	private ICallback saveHandler;

	@Override
	public Composite createContainer(Composite parent, ICallback handler) {
		display = new OdfTemplateFieldsDisplay(parent, handler);
		saveHandler = handler;
		return display;
	}
<<<<<<< HEAD

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		clear();
	}
=======
>>>>>>> branch 'ungrad-2024' of ssh://pinas:/git/elexis-ungrad-plugins

	@Override
	public boolean createEmptyDocument() {
		doc.clear();
		display.set(doc);
		return true;
	}

	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate) {
		if (asTemplate) {
			ByteArrayInputStream bais = new ByteArrayInputStream(bs);
			return loadFromStream(bais, asTemplate);
		} else {
			doc.setTemplate(bs, "");
			display.set(doc);
			return true;
		}
	}

	/**
	 * Load a letter or a template. Since we get a reference to a "Brief", we have
	 * access to informations like title and recipient
<<<<<<< HEAD
	 */

	public boolean loadFromBrief(Brief brief, boolean asTemplate) {
		try {
			if (asTemplate) {
				Object fields = doc.parseTemplate(brief);
				display.set(doc);
				if (fields != null) {
					return true;
				}
			} else {
				doc.setTemplate(brief.loadBinary(), brief.getBetreff());
				display.set(doc);
				return true;
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError("Fehler beim Laden", ex.getMessage());
		}
		return false;
	}

=======
	 * 
	 * @Override public boolean loadFromBrief(Brief brief, boolean asTemplate) { try
	 *           { if (asTemplate) { Object fields = doc.parseTemplate(brief);
	 *           display.set(doc); if (fields != null) { return true; } }else {
	 *           doc.setTemplate(brief.loadBinary(),brief.getBetreff());
	 *           display.set(doc); return true; } } catch (Exception ex) {
	 *           ExHandler.handle(ex); SWTHelper.showError("Fehler beim Laden",
	 *           ex.getMessage()); } return false; }
	 */
>>>>>>> branch 'ungrad-2024' of ssh://pinas:/git/elexis-ungrad-plugins
	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate) {
		try {
			if (asTemplate) {
				Object fields = doc.parseTemplate(is);
				display.set(doc);
				if (fields != null) {
					return true;
				}
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				FileTool.copyStreams(is, baos);
				display.set(null);
				return loadFromByteArray(baos.toByteArray(), false);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError("Fehler beim Lesen", ex.getMessage());
		}
		return false;
	}

	@Override
	public boolean findOrReplace(String pattern, ReplaceCallback cb) {
		boolean hadMatch = false;
		for (Entry<String, String> e : doc.getFields()) {
			String k = e.getKey();
			if (k.matches(pattern)) {
				hadMatch = true;
				doc.setField(k, (String) cb.replace(k));
			}
		}
		display.set(doc);
		return hadMatch;
	}

	@Override
	public byte[] storeToByteArray() {
		try {
			return doc.asByteArray();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError("Fehler beim Speichern", ex.getMessage());
			return new byte[0];
		}
	}

<<<<<<< HEAD
	@Override
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished) {
		// doc.doOutput();
		return false;
	}

=======
>>>>>>> branch 'ungrad-2024' of ssh://pinas:/git/elexis-ungrad-plugins
	@Override
	public boolean insertTable(String place, int properties, String[][] contents, int[] columnSizes) {
		StringBuffer sbu = new StringBuffer();
		for (int z = 0; z < contents.length; z++) {
			for (int s = 0; s < contents[z].length; s++) {
				sbu.append(contents[z][s]).append("\t");
			}
			sbu.append("\n");
		}
		String repl = sbu.toString();
		place = "\\[" + place.substring(1, place.length() - 1) + "\\]";
		Pattern pat = Pattern.compile(place);
		for (Entry<String, String> field : doc.getFields()) {
			Matcher matcher = pat.matcher(field.getKey());
			if (matcher.find()) {
				doc.setField(field.getKey(), repl);
			}
		}
		return true;
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
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_OPENOFFICE;
	}

<<<<<<< HEAD
	@Override
	public boolean isDirectOutput() {
		// TODO Auto-generated method stub
		return false;
	}

=======
>>>>>>> branch 'ungrad-2024' of ssh://pinas:/git/elexis-ungrad-plugins
	@Override
	public PageFormat getFormat() {
		return PageFormat.A4;
	}

	@Override
	public void setFormat(PageFormat f) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParameter(Parameter parameter) {
		// TODO Auto-generated method stub

	}

	@Override
<<<<<<< HEAD
	public void initTemplatePrintSettings(String template) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInitializationData(IConfigurationElement arg0, String arg1, Object arg2) throws CoreException {
		// TODO Auto-generated method stub

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
=======
>>>>>>> branch 'ungrad-2024' of ssh://pinas:/git/elexis-ungrad-plugins
	public int findCount(String text) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> findMatching(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getCurrentDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

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
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished) {
		// TODO Auto-generated method stub
		return false;
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
