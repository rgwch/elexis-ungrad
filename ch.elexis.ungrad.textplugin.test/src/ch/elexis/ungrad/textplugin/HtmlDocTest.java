package ch.elexis.ungrad.textplugin;

import java.io.File;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.junit.Test;

import ch.rgw.io.FileTool;

public class HtmlDocTest {

	@Test
	public void parse() throws Exception {
		HtmlDoc hDoc = new HtmlDoc();
		File test = new File("rsc", "test.html");
		String html = FileTool.readTextFile(test);
		hDoc.load(html.getBytes("utf-8"), true);
		for (Entry<String, String> e : hDoc.getPrefilled().entrySet()) {
			System.out.println(e.getKey() + "-> " + e.getValue());
		}
		hDoc.applyMatcher("\\[[\\w-\\s\\.]+\\]", t -> {
			return t.substring(1, t.length() - 1);
		});
		for (Entry<String, String> e : hDoc.getPrefilled().entrySet()) {
			System.out.println(e.getKey() + "-> " + e.getValue());
		}
		System.out.println(hDoc.text);
	}
	
	@Test
	public void insert() throws Exception{
		HtmlDoc hDoc = new HtmlDoc();
		File test = new File("rsc", "test.html");
		String html = FileTool.readTextFile(test);
		hDoc.load(html.getBytes("utf-8"), true);
		Object txm=hDoc.insertTextAt(10, 15, 20, 30, "Inserted", SWT.LEFT);
		Object txm2=hDoc.insertTextAt(txm, "Inserted after", SWT.RIGHT);
		FileTool.writeTextFile(new File("rsc","modified.html"), hDoc.text);
	}
	
	@Test
	public void convertPug() throws Exception{
		File test=new File("rsc","a5.pug");
		HtmlDoc doc=new HtmlDoc();
		String pug=FileTool.readTextFile(test);
		String html=doc.convertPug(pug);
		FileTool.writeTextFile(new File("rsc","pug.html"), html);
	}
}
