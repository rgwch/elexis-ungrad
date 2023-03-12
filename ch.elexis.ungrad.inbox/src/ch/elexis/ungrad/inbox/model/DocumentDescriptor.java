package ch.elexis.ungrad.inbox.model;

import java.io.File;

import ch.elexis.data.Person;
import ch.rgw.tools.TimeTool;

public class DocumentDescriptor {
	public Person concerns;
	public TimeTool docDate;
	public File file;
	public String filename;
	public DocumentDescriptor(Person c,TimeTool tt,File f, String fn) {
		concerns=c;
		docDate=new TimeTool(tt);
		file=f;
		filename=fn;
	}
}
