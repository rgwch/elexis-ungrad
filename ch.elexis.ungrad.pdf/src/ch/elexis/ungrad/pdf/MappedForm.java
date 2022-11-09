package ch.elexis.ungrad.pdf;

import java.util.HashMap;
import java.util.Map;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.Resolver;
import ch.rgw.tools.StringTool;

public class MappedForm {
	
	String fp;
	Manager mgr = new Manager();
	
	public MappedForm(String filepath){
		fp = filepath;
	}
	
	public String create(String outPath, String map, Patient pat) throws Exception{
		Resolver resolver = new Resolver();
		String raw = resolver.resolve(map);
		String[] lines = raw.split("\\R");
		Map<String, String> mappings = new HashMap<String, String>();
		for (String line : lines) {
			String[] val = line.split(":");
			if (val.length>1 && !StringTool.isNothing(val[1].trim())) {
				mappings.put(val[0].trim(), val[1].trim());
			}
		}
		return mgr.fillForm(fp, outPath, mappings);
	}
}
