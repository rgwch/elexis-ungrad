/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
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
package ch.elexis.ungrad;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.text.Messages;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Person;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Resolve Elexis-Variables such as [Patient.Name] with the currently selected
 * respective items
 * 
 * @author gerry
 *
 */
public class Resolver {
	private static Logger log = LoggerFactory.getLogger(Resolver.class);
	@Reference
	private IContextService contextService=ContextServiceHolder.get();
	
	Map<String, IPersistentObject> replmap;
	boolean bAsHtml = false;

	/**
	 * Create a Resolver with some additional information how to resolve
	 * non-standard-classes
	 * 
	 * @param fld    Map Variable Names to classes.
	 * @param asHTML if true replace linefeeds (\n) with html linebreaks (<br />
	 *               )
	 */
	public Resolver(Map<String, IPersistentObject> fld, boolean asHTML) {
		replmap = fld;
		bAsHtml = asHTML;
	}

	public Resolver() {
		this(new HashMap<>(), false);
	}

	public void asHTML(boolean html) {
		this.bAsHtml = html;
	}

	/**
	 * Resolve Variables in a String
	 * 
	 * @param raw The input String containing Placeholders
	 * @return The String with resolved placeholders
	 * @throws Exception If a Var references a class that could not be found in the
	 *                   running Elexis installation.
	 */

	public String resolve(String raw) throws Exception {
		String level1 = resolveSimple(raw);
		String level2 = resolveGenderized(level1);
		return level2;
	}

	public String resolveSimple(String raw) throws Exception {
		Pattern pat = Pattern.compile(TextContainer.MATCH_TEMPLATE);
		StringBuffer sb = new StringBuffer();
		Matcher matcher = pat.matcher(raw);
		while (matcher.find()) {
			String found = matcher.group();
			String replacement = replaceTemplate(found);
			if (!replacement.startsWith("**ERROR")) {
				matcher.appendReplacement(sb, replacement);
			} else {
				matcher.appendReplacement(sb, " ");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public String resolveGenderized(String raw) throws Exception {
		Pattern pat = Pattern.compile(TextContainer.MATCH_GENDERIZE);
		StringBuffer sb = new StringBuffer();
		Matcher matcher = pat.matcher(raw);
		while (matcher.find()) {
			String found = matcher.group();
			String replacement = genderize(found);
			if (!replacement.startsWith("**ERROR")) {
				matcher.appendReplacement(sb, replacement);
			} else {
				matcher.appendReplacement(sb, " ");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Replace Template Fields like [Patient.Name]
	 * 
	 * @param tmpl
	 * @return
	 * @throws ClassNotFoundException
	 */
	private String replaceTemplate(String tmpl) throws ClassNotFoundException {
		String[] rooted = tmpl.substring(1, tmpl.length() - 1).split("\\.");
		if (rooted.length == 2) {
			if (rooted[0].equals("Datum")) {
				return new TimeTool().toString(TimeTool.DATE_GER);
			} else {
				Optional<IPersistentObject> po = resolveObject(rooted[0]);
				if (po.isEmpty()) {
					return "";
				}
				String r = po.get().get(rooted[1]);
				String replacement = StringTool.unNull(r);
				if (bAsHtml) {
					replacement = replacement.replaceAll("\\R", "<br />");
				}
				return replacement;
			}
		} else {
			return tmpl;
		}
	}

	private Optional<IPersistentObject> resolveObject(String name) {
		IPersistentObject rep=replmap.get(name);
		Optional<IPersistentObject> po = Optional.ofNullable(rep);
		if (po.isEmpty()) {
			String fqname = "ch.elexis.data." + name; //$NON-NLS-1$
			try {
				Class clazz=Class.forName(fqname);
				po = contextService.getTyped(clazz);
						// ElexisEventDispatcher.getSelected(Class.forName(fqname));
			} catch (ClassNotFoundException cfe) {
				return null;
			}
		}
		return po;
	}

	/**
	 * Format für Genderize: [Feld:mw:formulierung Mann/formulierung Frau] oder
	 * [Feld:mwn:mann/frau/neutral]
	 */
	private String genderize(final String in) {
		String inl = in.substring(1,in.length()-1);
		boolean showErrors = true;
		if (inl.substring(0, 1).equalsIgnoreCase("*")) {
			inl = inl.substring(1);
			showErrors = false;
		}
		String[] q = inl.split(":"); //$NON-NLS-1$
		Optional<IPersistentObject> o = resolveObject(q[0]);
		if (o.isEmpty()) {
			if (showErrors) {
				return "???";
			} else {
				return "";
			}
		}
		if (q.length != 3) {
			log.error("falsches genderize Format " + inl); //$NON-NLS-1$
			return null;
		}
		if (!(o.get() instanceof Kontakt)) {
			if (showErrors) {
				return Messages.TextContainer_FieldTypeForContactsOnly;
			} else {
				return "";
			}
		}
		Kontakt k = (Kontakt) o.get();
		String[] g = q[2].split("/"); //$NON-NLS-1$
		if (g.length < 2) {
			if (showErrors) {
				return Messages.TextContainer_BadFieldDefinition;
			} else {
				return "";
			}
		}
		if (k.istPerson()) {
			Person p = Person.load(k.getId());

			if (p.get(Person.SEX).equals(Person.MALE)) {
				if (q[1].startsWith("m")) { //$NON-NLS-1$
					return g[0];
				}
				return g[1];
			} else {
				if (q[1].startsWith("w")) { //$NON-NLS-1$
					return g[0];
				}
				return g[1];
			}
		} else {
			if (g.length < 3) {
				if (showErrors) {
					return Messages.TextContainer_FieldTypeForPersonsOnly;
				} else {
					return "";
				}
			}
			return g[2];
		}
	}

}
