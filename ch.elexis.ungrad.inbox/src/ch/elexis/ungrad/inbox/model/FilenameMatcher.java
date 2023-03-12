package ch.elexis.ungrad.inbox.model;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.rgw.tools.TimeTool;

public class FilenameMatcher {
	Pattern datePattern;

	public FilenameMatcher() {
		datePattern = Pattern.compile("\\d{2,4}[\\.-]\\d\\d[\\.-]\\d{2,4}");
	}

	public String convert(final File file) {
		String ret = file.getName();
		ret=findDatesInString(ret);
		int ext=ret.lastIndexOf('.');
		if(ext>-1) {
			ret=ret.substring(0,ext)+ret.substring(ext).toLowerCase();
		}
		return ret;
	}

	public String findDatesInString(final String input) {
		TimeTool now = new TimeTool();
		TimeTool cand = new TimeTool();
		int start = -1;
		int end = -1;
		Matcher m = datePattern.matcher(input);
		while (m.find()) {
			TimeTool t = new TimeTool(m.group());
			if (t.isBeforeOrEqual(now)) {
				if (cand.isEqual(now) || t.isAfter(cand)) {
					cand = t;
					start = m.start();
					end = m.end();
				}
			}
		}
		String ret = input;
		if (start > -1) {
			ret = ret.substring(0, start) + ret.substring(end);
		}
		if (ret.startsWith("-") || ret.startsWith("_")) {
			ret = ret.substring(1);
		}
		ret=ret.replaceAll("\\(\\)", "");
		return cand.toString(TimeTool.DATE_ISO) + "_" + ret.trim();
	}
}
