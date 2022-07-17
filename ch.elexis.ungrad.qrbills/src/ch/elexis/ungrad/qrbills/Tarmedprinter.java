/*******************************************************************************
 * Copyright (c) 2018-2022 by G. Weirich
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
package ch.elexis.ungrad.qrbills;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.data.Rechnung;
import ch.elexis.ungrad.Resolver;

public class Tarmedprinter {
	private Resolver resolver;
	public static final Namespace nsinvoice = Namespace.getNamespace("invoice",
			"http://www.forum-datenaustausch.ch/invoice");

	public Tarmedprinter(Resolver resolver) {
		this.resolver = resolver;
	}

	public boolean print(Rechnung rn, File xmlPath, IRnOutputter.TYPE type) {
		XMLExporter xmlex = new XMLExporter();
		Document xml = xmlex.doExport(rn, xmlPath.getAbsolutePath(), type, false);
		if (xml != null) {
			Element root = xml.getRootElement();
			Element payload = root.getChild("payload", nsinvoice);
			Element body = payload.getChild("body", nsinvoice);
			Element prolog = body.getChild("prolog", nsinvoice);
			Element balance = body.getChild("balance", nsinvoice);
			Element esr9 = body.getChild("esr9", nsinvoice);
			Element tg = body.getChild("tiers_garant", nsinvoice);
			Element tp = body.getChild("tiers_payant", nsinvoice);
			Element treatement = body.getChild("treatment", nsinvoice);
			Element services = body.getChild("services", nsinvoice);
		}
		return false;
	}

}
