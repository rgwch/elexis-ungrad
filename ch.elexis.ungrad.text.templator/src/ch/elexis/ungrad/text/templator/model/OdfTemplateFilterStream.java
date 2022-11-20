package ch.elexis.ungrad.text.templator.model;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ch.rgw.tools.StringTool;

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
					for (int c : val.getBytes()) {
						super.write(c);
					}
				}

				sb = null;
				state = AWAIT_START;
			}
			break;
		}
	}
}
