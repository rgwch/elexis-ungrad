package ch.elexis.ungrad.QR_Outputter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.ungrad.QR_Outputter.messages"; //$NON-NLS-1$

	// BillPreferencePage
	public static String BillPreferencePage_Title;
	public static String BillPreferencePage_SenderLine;
	public static String BillPreferencePage_SendMailIfCaseVar;
	public static String BillPreferencePage_MessageTitle;
	public static String BillPreferencePage_MessageBody;

	// QrRnOutputter
	public static String QrRnOutputter_Description;
	public static String QrRnOutputter_ErrorTitle;
	public static String QrRnOutputter_NoXMLDir;
	public static String QrRnOutputter_NoPDFDir;
	public static String QrRnOutputter_ExportingInvoices;
	public static String QrRnOutputter_TransmissionError;
	public static String QrRnOutputter_DefectiveInvoices;
	public static String QrRnOutputter_TransmissionComplete;
	public static String QrRnOutputter_NoErrors;
	public static String QrRnOutputter_OutputError;
	public static String QrRnOutputter_CouldNotStart;
	public static String QrRnOutputter_WriteError;
	public static String QrRnOutputter_CouldNotWrite;
	public static String QrRnOutputter_ByMailTo;

	// QR_SettingsControl
	public static String QR_SettingsControl_OutputDespiteMissingData;
	public static String QR_SettingsControl_PDFDirectory;
	public static String QR_SettingsControl_XMLDirectory;
	public static String QR_SettingsControl_Change;
	public static String QR_SettingsControl_PrintQRPage;
	public static String QR_SettingsControl_PrintInvoiceForm;
	public static String QR_SettingsControl_PrintOrMail;
	public static String QR_SettingsControl_FaceDown;
	public static String QR_SettingsControl_DirectPrintOn;
	public static String QR_SettingsControl_DeleteAfterPrint;
	public static String QR_SettingsControl_DebugKeepHTML;

	// Common UI labels
	public static String Core_Print;
	public static String Core_Open;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
