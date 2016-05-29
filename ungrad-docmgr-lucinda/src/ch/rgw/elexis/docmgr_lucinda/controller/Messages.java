package ch.rgw.elexis.docmgr_lucinda.controller;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.rgw.elexis.docmgr_lucinda.controller.messages"; //$NON-NLS-1$
	public static String Controller_cons_not_found_caption;
	public static String Controller_cons_not_found_text;
	public static String Controller_could_not_launch_file;
	public static String Controller_omnivore_not_found_caption;
	public static String Controller_omnivore_not_found_text;
	public static String Controller_unknown_type_caption;
	public static String Controller_unknown_type_text;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
