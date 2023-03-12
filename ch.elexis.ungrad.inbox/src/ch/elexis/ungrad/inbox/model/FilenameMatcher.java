package ch.elexis.ungrad.inbox.model;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.rgw.tools.TimeTool;

public class FilenameMatcher {
	Pattern datePattern;
	
	public FilenameMatcher(){
		datePattern=Pattern.compile("\\d{2,4}[\\.-]\\d\\d[\\.-]\\d{2,4}");
	}
	public String convert(final File file) {
		String ret=file.getName();
		TimeTool[] dates=findDatesInString(ret);
		return ret;
	}
	
	public TimeTool[] findDatesInString(final String input) {
		ArrayList<TimeTool> ret=new ArrayList<TimeTool>();
		Matcher m=datePattern.matcher(input);
		while(m.find()) {
			ret.add(new TimeTool(m.group()));
		}
		return (TimeTool[]) ret.toArray(new TimeTool[0]);
	}
}
