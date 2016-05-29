package ch.rgw.elexis.docmgr_lucinda.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.rgw.elexis.docmgr_lucinda.model.messages"; //$NON-NLS-1$
	public static String DateParser_unknown_date_format;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
