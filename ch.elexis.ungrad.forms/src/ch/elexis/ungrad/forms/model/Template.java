package ch.elexis.ungrad.forms.model;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.ungrad.Resolver;
import ch.rgw.tools.StringTool;

public class Template {
	
	private String html;
	Document doc;
	String title = "";
	String heading = "";
	String doctype = "";
	String filename;
	Kontakt adressat = null;
	Map<String, String> inputs = new LinkedHashMap<String, String>();
	
	public Template(String rawhtml, Kontakt adressat) throws Exception{
		Map<String, IPersistentObject> replacer = new HashMap<>();
		if (adressat != null) {
			this.adressat = adressat;
			replacer.put("Adressat", adressat);
		}
		Resolver resolver = new Resolver(replacer, true);
		this.html = resolver.resolve(rawhtml);
		
		doc = Jsoup.parse(html);
		Document.OutputSettings outs = doc.outputSettings();
		outs.prettyPrint(false);
		outs.syntax(Document.OutputSettings.Syntax.xml);
		Elements els = doc.getElementsByTag("title");
		Element eTitle = els.first();
		if (eTitle != null) {
			title = eTitle.text();
		}
		els = doc.getElementsByTag("h1");
		Element eHeader = els.first();
		if (eHeader != null) {
			heading = eHeader.text();
		}
		els = doc.getElementsByAttribute("data-anrede");
		Element eAnrede = els.first();
		if (eAnrede != null && adressat != null) {
			String bem=adressat.getBemerkung();
			Pattern pat=Pattern.compile(":Anrede:([^:]+):");
			Matcher m=pat.matcher(bem);
			
			if(m.find()) {
				String anrede=m.group(1);
				eAnrede.html(anrede);
				html=doc.html();
			}
		}
		els = doc.getElementsByAttribute("data-doctype");
		Element eDoctype = els.first();
		if (eDoctype != null) {
			doctype = eDoctype.attr("data-doctype");
		}
		els = doc.getElementsByAttribute("data-doctitle");
		eTitle = els.first();
		if (eTitle != null) {
			title = eTitle.attr("data-doctitle");
		}
		if (StringTool.isNothing(title)) {
			title = heading;
		}
		if (StringTool.isNothing(title)) {
			title = doctype;
		}
		els = doc.getElementsByAttribute("data-input");
		Iterator<Element> it = els.iterator();
		while (it.hasNext()) {
			Element input = it.next();
			inputs.put(input.attr("data-input"), input.html());
		}
	}
	
	public void setInput(String key, String value){
		inputs.put(key, value);
		Elements els = doc.getElementsByAttributeValue("data-input", key);
		els.html(value);
		html = doc.html();
	}
	
	public void replace(String orig, String replacement){
		html = doc.html().replace(orig, replacement);
		doc = Jsoup.parse(html);
	}
	
	public String getXml(){
		Document.OutputSettings settings = doc.outputSettings();
		settings.syntax(Document.OutputSettings.Syntax.xml);
		doc.outputSettings(settings);
		return doc.html();
	}
	
	public String getDoctype(){
		return doctype;
	}
	
	public String getTitle(){
		return title;
	}
	
	public Map<String, String> getInputs(){
		return inputs;
	}
	
	public void setFilename(String absoluteFile){
		this.filename = absoluteFile;
	}
	
	public String getFilename(){
		return this.filename;
	}
}
