package ch.elexis.ungrad.textplugin;

import java.util.Map.Entry;

import org.junit.Test;

public class HtmlDocTest {
	
	@Test
	public void parse() throws Exception{
		HtmlDoc hDoc=new HtmlDoc();
		hDoc.loadTemplate("test.html","rsc");
		for(Entry<String, String> e: hDoc.prefilled.entrySet()) {
			System.out.println(e.getKey()+"-> "+e.getValue());
		}
		hDoc.applyMatcher("\\[[\\w-\\s\\.]+\\]", t->{
			return t.substring(1,t.length()-1);
		});
		for(Entry<String, String> e: hDoc.prefilled.entrySet()) {
			System.out.println(e.getKey()+"-> "+e.getValue());
		}
		System.out.println(hDoc.compile());
	}
}
