package ch.elexis.ungrad.textplugin;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.ui.text.ITextPlugin;

public class TextPlugin implements ITextPlugin {

	private PageFormat format=PageFormat.A4;
	private Parameter param;
	
	@Override
	public PageFormat getFormat() {
		return format;
	}

	@Override
	public void setFormat(PageFormat f) {
		format=f;
	}

	@Override
	public void setParameter(Parameter parameter) {
		param=parameter;
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
	public boolean createEmptyDocument() {
		
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

	@Override
	public Composite createContainer(Composite parent, ICallback handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitializationData(IConfigurationElement arg0, String arg1, Object arg2) throws CoreException {
		// TODO Auto-generated method stub
		
	}

}
