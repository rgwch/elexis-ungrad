/**
 * Copyright (c) 2022-2024, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 */

package ch.elexis.ungrad.text.templator.model;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ch.rgw.tools.StringTool;

/**
 * A Filter Stream that looks for [variables] and replaces them with their computed values 
 */
public class OdfTemplateFilterStream extends FilterOutputStream {
	private StringBuilder sb;
	private static final int AWAIT_START = 0;
	private static final int AWAIT_END = 1;

	private int state = AWAIT_START;
	private ODFDoc doc;

	public OdfTemplateFilterStream(ODFDoc doc, OutputStream out) {
		super(out);
		this.doc = doc;

	}

	@Override
	public void flush() throws IOException {
		if (sb != null) {
			for (int i = 0; i < sb.length(); i++) {
				super.write(sb.charAt(i));
			}
			sb = null;
			state = AWAIT_START;
		}
		super.flush();
	}

	/**
	 * We wait for the character '['. If found, collect all characters until a ']' is found.
	 * Then, find the field in the document matching the found variable name and write its content into the stream.
	 */
	@Override
	public void write(int b) throws IOException {
		switch (state) {
		case AWAIT_START:
			if (b == '[') {
				sb = new StringBuilder("[");
				state = AWAIT_END;
			} else {
				super.write(b);
			}
			break;

		case AWAIT_END:
			sb.append((char) b);
			if (b == ']') {
				String field = sb.toString();
				String val = doc.getField(field);
				if (StringTool.isNothing(val)) {
					flush();
				} else {
					String conv = convert(val);
					for (int c : conv.getBytes()) {
						super.write(c);
					}
				}

				sb = null;
				state = AWAIT_START;
			}
			break;
		}
	}

	private String convert(String input) {
		String ret = input.replaceAll("\\t", "<text:tab/>");
		ret = ret.replaceAll("\\n", "<text:line-break/>");
		return ret;
	}
}
