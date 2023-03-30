
/*******************************************************************************
 * Copyright (c) 2023, G. Weirich and Elexis
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

import ch.elexis.data.Person;
import ch.rgw.tools.TimeTool;

public class DocumentDescriptor {
	public Person concerns;
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
	public DocumentDescriptor(Person c,TimeTool tt,File f, String fn) {
		concerns=c;
		docDate=new TimeTool(tt);
		file=f;
		filename=fn;
	}
}
