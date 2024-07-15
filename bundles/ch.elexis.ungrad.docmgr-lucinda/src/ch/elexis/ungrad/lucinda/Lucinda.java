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
package ch.elexis.ungrad.lucinda;

import java.io.IOException;
import java.util.Map;

import ch.rgw.io.FileTool;

public class Lucinda {
	private Client3 client;
	private boolean connected = false;
	
	public Lucinda(){
		client = new Client3();
	}
	
	public void rescan(){
		client.rescan();
	}
	
	public Map query(String q) throws Exception{
		return client.query(q);
	}
	
	public Map get(String id) throws Exception{
		return client.get(id);
	}
	
	public Map addToIndex(String id, String title, String type, Map meta, byte[] contents,
		boolean bCopy) throws Exception{
		if (bCopy) {
			String filetype = (String) meta.get("filetype");
			if (filetype != null) {
				String ext = FileTool.getExtension(filetype);
				if (ext.length() == 0) {
					ext = filetype;
				}
				title += "." + ext;
			}
			return client.addFile(id, title, (String) meta.get("concern"), type, meta, contents);
		} else {
			return client.addToIndex(id, title, type, meta, contents);
		}
	}
	
}
