package ch.elexis.ungrad.forms.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.forms.ui.messages"; //$NON-NLS-1$
	public static String DetailDisplay_OutputError;
	public static String DocumentList_CreateError;
	public static String DocumentList_MessageBodyDefault;
	public static String DocumentList_PrintError;
	public static String FormsExtension_Confirm;
	public static String FormsExtension_DeleteAllFiles;
	public static String FormsExtension_ErrorRemovingReference;
	public static String SelectTemplateDialog_SelectForm;
	public static String View_CantCreateOutputDir;
	public static String View_Completion;
	public static String View_Create;
	public static String View_CreateAndOutput;
	public static String View_CreateNewDocument;
	public static String View_Delete_Header;
	public static String View_Delete_Text;
	public static String View_DeleteDocument;
	public static String View_Doclist;
	public static String View_Documents;
	public static String View_ErrorDeleting;
	public static String View_ErrorProcessing;
	public static String View_ErrorSaving;
	public static String View_Form;
	public static String View_New;
	public static String View_Output_Heading;
	public static String View_Output_Text;
	public static String View_OutputError;
	public static String View_PleaseConfirm;
	public static String View_PleaseSelectReceiver;
	public static String View_ReallyDelete;
	public static String View_Receiver;
	public static String View_Send_Header;
	public static String View_SendAsPDFByMail;
	public static String View_SendByMail;
	public static String View_ShowCurrentForm;
	public static String View_ShowListOfDocuments;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
