/*******************************************************************************
 * Copyright (c) 2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.ungrad.forms.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.ungrad.Resolver;
import ch.rgw.tools.StringTool;

/**
 * In-Memory representation of a partly processed HTML template.
 * @author gerry
 *
 */
public class Template {

	private String html;
	Document doc;
	String title = "";
	String heading = "";
	String doctype = "";
	String mailSender = "";
	String mailBody = "";
	String mailSubject = "";
	String mailRecipient = "";
	String filename;
	private Kontakt adressat = null;
	Map<String, String> inputs = new LinkedHashMap<String, String>();

	/**
	 * Analyze a HTML template and prefill some elements with the given recipient 
	 * @param rawhtml unprocessed HTML, possibly with variable fields.
	 * @param adressat the recipient
	 * @throws Exception
	 */
	public Template(String rawhtml, Kontakt adressat) throws Exception {
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
			String bem = adressat.getBemerkung();
			Pattern pat = Pattern.compile(":Anrede:([^:]+):");
			Matcher m = pat.matcher(bem);

			if (m.find()) {
				String anrede = m.group(1);
				eAnrede.html(anrede);
				html = doc.html();
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
		els = doc.getElementsByAttribute("data-mail");
		it = els.iterator();
		while (it.hasNext()) {
			Element mailpart = it.next();
			String type = mailpart.attr("data-mail");
			if (type.equals("sender")) {
				mailSender = mailpart.text();
			} else if (type.equals("body")) {
				mailBody = mailpart.html();
			} else if (type.equals("subject")) {
				mailSubject = mailpart.text();
			} else if (type.equals("recipient")) {
				mailRecipient = mailpart.text();
			}
		}
	}

	/**
	 * Retrieve the recipient of documents created from this template. Will set "x-adressat"-elements in the HTML with
	 * any found receiver
	 * @return the receiver or null, if none defined
	 */
	public Kontakt getAdressat() {
		Element body = doc.body();
		Element eAdressat = body.getElementById("x-adressat");
		if (eAdressat == null) {
			if (adressat == null) {
				Brief brief = getBrief();
				if (brief != null) {
					setAdressat(brief.getAdressat());
				}
			} else {
				setAdressat(adressat);
			}
		} else {
			String id = eAdressat.attr("data-id");
			Kontakt ka = Kontakt.load(id);
			if (ka.isValid()) {
				adressat = ka;
			}
		}
		return adressat;
	}

	/**
	 * Set the intended recipient for documents created with this template. Will set "x-adressat"- fields in the HTML
	 * to persist the recipient.
	 * @param adr the recipient
	 */
	public void setAdressat(Kontakt adr) {
		if (adressat != null && adressat.isValid()) {
			adressat = adr;
			Element eAdressat = doc.getElementById("x-adressat");
			if (eAdressat == null) {
				doc.body().append("<span id=\"x-adressat\" data-id=\"" + adressat.getId() + "\"></span>");
			} else {
				eAdressat.attr("data-id", adressat.getId());
			}
			html = doc.html();
		}
	}

	/**
	 * Set the Elexis "Brief" connected do the document created with this template. Will also create "x-brief" element
	 * to persist the information.
	 * @param brief
	 */
	public void setBrief(Brief brief) {
		Element body = doc.body();
		Element eBrief = body.getElementById("x-brief");
		if (eBrief == null) {
			body.append("<span id=\"x-brief\" data-id=\"" + brief.getId() + "\"></span>");
		} else {
			eBrief.attr("data-id", brief.getId());
		}
		html = doc.html();
	}

	/**
	 * Find the Elexis "Brief" linked to this template, if any
	 * @return the Brief or null.
	 */
	public Brief getBrief() {
		Element body = doc.body();
		Element eBrief = body.getElementById("x-brief");
		if (eBrief != null) {
			String bid = eBrief.attr("data-id");
			Brief ret = Brief.load(bid);
			if (ret.isValid()) {
				return ret;
			}
		}
		return null;
	}

	/**
	 * Get the Mail sender for this template
	 * @return
	 */
	public String getMailSender() {
		return mailSender;
	}

	/**
	 * Get The subject for mails created with this template
	 * @return
	 */
	public String getMailSubject() {
		return mailSubject;
	}

	/**
	 * Get the body for mails created witg this subject. 
	 * @return
	 */
	public String getMailBody() {
		if (StringTool.isNothing(mailBody)) {
			return CoreHub.localCfg.get(PreferenceConstants.MAIL_BODY, "Siehe Anhang");
		} else {
			return mailBody;
		}
	}

	public String getMailRecipient() {
		adressat = getAdressat();
		if (adressat == null) {
			return mailRecipient;
		} else {
			String ret = adressat.get(Kontakt.FLD_E_MAIL);
			if (StringTool.isNothing(ret)) {
				return mailRecipient;
			} else {
				return ret;
			}
		}
	}

	/**
	 * Replace an input field in the HTML with an entry.
	 * @param key id of the input field
	 * @param value value to place there (can cpntain HTML))
	 */
	public void setInput(String key, String value) {
		inputs.put(key, value);
		Elements els = doc.getElementsByAttributeValue("data-input", key);
		els.html(value);
		html = doc.html();
	}

	/**
	 * Replace any text in the template with a value. Will replace all occurences
	 * @param orig text to search
	 * @param replacement new Text
	 */
	public void replace(String orig, String replacement) {
		html = doc.html().replace(orig, replacement);
		doc = Jsoup.parse(html);
	}

	/**
	 * Return the template as XHTML
	 * @return
	 */
	public String getXml() {
		Document.OutputSettings settings = doc.outputSettings();
		settings.syntax(Document.OutputSettings.Syntax.xml);
		doc.outputSettings(settings);
		return doc.html();
	}

	public String getDoctype() {
		return doctype;
	}

	public String getTitle() {
		return title;
	}

	public Map<String, String> getInputs() {
		return inputs;
	}

	/**
	 * Set the filenome for a document created with this template
	 * @param absoluteFile An absolute pathname for the output file
	 */
	public void setFilename(String absoluteFile) {
		this.filename = absoluteFile;
	}

	/**
	 * Get the absolute Pathname for documents created with this template
	 * @return
	 */
	public String getFilename() {
		return this.filename;
	}
}
