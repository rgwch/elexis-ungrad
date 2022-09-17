/*******************************************************************************
 * Copyright (c) 2016-2022 by G. Weirich
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.PersistentObject;
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
	Map<String, IPersistentObject> replmap;
	boolean html = false;

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
		html = asHTML;
	}

	public Resolver() {
		this(new HashMap<>(), false);
	}

	public void asHTML(boolean html) {
		this.html = html;
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

	private String replaceTemplate(String tmpl) throws ClassNotFoundException {
		String[] rooted = tmpl.substring(1, tmpl.length() - 1).split("\\.");
		if (rooted.length == 2) {
			if (rooted[0].equals("Datum")) {
				return new TimeTool().toString(TimeTool.DATE_GER);
			} else {
				IPersistentObject po = replmap.get(rooted[0]);
				if (po == null) {
					String fqname = "ch.elexis.data." + rooted[0]; //$NON-NLS-1$
					try {
						po = ElexisEventDispatcher.getSelected(Class.forName(fqname));
					} catch (ClassNotFoundException cfe) {
						return "";
					}
				}
				String r = po.get(rooted[1]);
				String replacement = StringTool.unNull(r);
				if (html) {
					replacement = replacement.replaceAll("\\n", "<br />");
				}
				return replacement;
			}
		} else {
			return tmpl;
		}
	}
}
