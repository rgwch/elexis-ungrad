package ch.elexis.ungrad.forms.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.forms.model.messages"; //$NON-NLS-1$
	public static String Controller_11;
	public static String Controller_12;
	public static String Controller_13;
	public static String Controller_21;
	public static String Controller_6;
	public static String Controller_7;
	public static String Controller_CouldNotCreateDir;
	public static String Controller_CouldNotCreateFile;
	public static String Controller_ErrorReadingDirectory;
	public static String Controller_Output;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
