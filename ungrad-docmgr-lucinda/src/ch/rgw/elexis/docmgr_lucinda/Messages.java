package ch.rgw.elexis.docmgr_lucinda;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.rgw.elexis.docmgr_lucinda.messages"; //$NON-NLS-1$
	public static String Activator_14;
	public static String Activator_Lucinda_error_caption;
	public static String Activator_Server_message;
	public static String Activator_Server_Message;
	public static String Activator_unexpected_answer;
	public static String Preferences_Inbox_Name;
	public static String Preferences_Konsultation_Name;
	public static String Preferences_Omnivore_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
