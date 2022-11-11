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

package ch.elexis.ungrad.pdf;

import java.util.HashMap;
import java.util.Map;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.Resolver;
import ch.rgw.tools.StringTool;

/**
 * Handle pdf forms with mapping files. The map is a String field with lines such as
 * PdfFieldName:ElexisFieldName, e.g. lastname:[Patient.Name]
 * @author gerry
 *
 */
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
