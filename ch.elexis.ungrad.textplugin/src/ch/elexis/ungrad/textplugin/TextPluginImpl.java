/*******************************************************************************
 * Copyright (c) 2022 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad.textplugin;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.tools.ExHandler;

public class TextPluginImpl implements ITextPlugin {
	
	private PageFormat format = PageFormat.A4;
	private Parameter param;
	private HtmlDoc doc = new HtmlDoc();
	private HtmlProcessorDisplay display;
	
	@Override
	public PageFormat getFormat(){
		return format;
	}
	
	@Override
	public void setFormat(PageFormat f){
		format = f;
	}
	
	@Override
	public void setParameter(Parameter parameter){
		param = parameter;
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void showMenu(boolean b){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void showToolbar(boolean b){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSaveOnFocusLost(boolean bSave){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean createEmptyDocument(){
		try {
			doc.loadTemplate("frame.html", "");
			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
			return false;
		}
	}
	
	@Override
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate){
		try {
			doc.load(bs);
			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
			return false;
		}
	}
	
	@Override
	public boolean loadFromStream(InputStream is, boolean asTemplate){
		byte[] daten = null;
		try {
			daten = new byte[is.available()];
			is.read(daten);
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return loadFromByteArray(daten, asTemplate);
	}
	
	@Override
	public boolean findOrReplace(String pattern, ReplaceCallback cb){
		doc.applyMatcher(pattern, cb);
		return true;
		/*
		Pattern pat = Pattern.compile(pattern);
		StringBuffer sb = new StringBuffer();
		Matcher matcher = pat.matcher(doc.orig);
		while (matcher.find()) {
			String found = matcher.group();
			String replacement = (String) cb.replace(found);
			if (!replacement.startsWith("**ERROR")) {
				matcher.appendReplacement(sb, replacement);
				doc.addField(found, replacement);
			} else {
				matcher.appendReplacement(sb, " ");
			}
		}
		matcher.appendTail(sb);
		return false;
		*/
	}
	
	@Override
	public byte[] storeToByteArray(){
		return doc.storeToByteArray();
	}
	
	@Override
	public boolean insertTable(String place, int properties, String[][] contents,
		int[] columnSizes){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean setFont(String name, int style, float size){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean setStyle(int style){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object insertText(String marke, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object insertText(Object pos, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean clear(){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished){
		Manager pdfManager = new Manager();
		
		return false;
	}
	
	@Override
	public String getMimeType(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isDirectOutput(){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void initTemplatePrintSettings(String template){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Composite createContainer(Composite parent, ICallback handler){
		display = new HtmlProcessorDisplay(parent, doc, handler);
		return display;
	}
	
	@Override
	public void setInitializationData(IConfigurationElement arg0, String arg1, Object arg2)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
}
