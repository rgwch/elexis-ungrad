package ch.elexis.ungrad.forms.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.forms.ui.messages"; //$NON-NLS-1$
	public static String DetailDisplay_OutputError;
	public static String DocumentList_CreateError;
	public static String DocumentList_MessageBodyDefault;
	public static String DocumentList_PrintError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
