
/*******************************************************************************
 * Copyright (c) 2023-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.ungrad.inbox.model;

import java.io.File;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class DocumentDescriptor {
	public String concerns_id;
	public TimeTool docDate;
	public File file;
	public String filename;
	public String sender;
	public String subject;
	public String lastname;
	public String firstname;
	public String fullname;
	public String docname;
	public TimeTool dob;

	public DocumentDescriptor(TimeTool tt, File f, String fn) {
		docDate = new TimeTool(tt);
		file = f;
		filename = fn;
	}

	public DocumentDescriptor(Person c, TimeTool tt, File f, String fn) {
		concerns_id = c.getId();
		docDate = new TimeTool(tt);
		file = f;
		filename = fn;
	}

	public DocumentDescriptor(IPerson c, TimeTool tt, File f, String fn) {
		concerns_id = c.getId();
		docDate = new TimeTool(tt);
		file = f;
		filename = fn;
	}

	public boolean concerns() {
		return !StringTool.isNothing(concerns_id);
	}

	public void concern(IPerson p) {
		concerns_id = p != null ? p.getId() : null;
	}

	public void concern(IPatient p) {
		concerns_id = p != null ? p.getId() : null;
	}

	public void concern(Person p) {
		concerns_id = p != null ? p.getId() : null;
	}

	public void concern(Patient p) {
		concerns_id = p != null ? p.getId() : null;
	}
}
