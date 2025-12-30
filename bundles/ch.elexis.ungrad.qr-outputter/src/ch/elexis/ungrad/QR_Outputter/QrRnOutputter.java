package ch.elexis.ungrad.QR_Outputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.progress.IProgressService;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.LoggerFactory;

import ch.elexis.TarmedRechnung.XMLExporter;
import ch.elexis.TarmedRechnung.XMLExporterUtil;
import ch.elexis.base.ch.arzttarife.xml.exporter.Tarmed45Exporter.EsrType;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.InvoiceConstants;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.InvoiceState.REJECTCODE;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.rechnung.RnOutputDialog;
import ch.elexis.core.utils.PlatformHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Rechnung;
import ch.elexis.pdfBills.ElexisPDFGenerator;
import ch.elexis.pdfBills.OutputterUtil;
import ch.elexis.pdfBills.PdfUtil;
import ch.elexis.pdfBills.TarmedXmlUtil;
import ch.elexis.ungrad.Mailer;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.pdf.Manager;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class QrRnOutputter implements IRnOutputter {
	public static final String PDFDIR = "pdfdir"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "ch.elexis.pdfBills"; //$NON-NLS-1$
	public static final String XMLDIR = "xmldir"; //$NON-NLS-1$
	public static final String CFG_ROOT = "qrpdf-output/"; //$NON-NLS-1$
	public static final String CFG_MARGINLEFT = "margin.left"; //$NON-NLS-1$
	public static final String CFG_MARGINRIGHT = "margin.right"; //$NON-NLS-1$
	public static final String CFG_MARGINTOP = "margin.top"; //$NON-NLS-1$
	public static final String CFG_MARGINBOTTOM = "margin.bottom"; //$NON-NLS-1$
	public static final String CFG_BESR_MARGIN_VERTICAL = "margin.besr.vertical"; //$NON-NLS-1$
	public static final String CFG_BESR_MARGIN_HORIZONTAL = "margin.besr.horizontal"; //$NON-NLS-1$

	public static final String CFG_ESR_HEADER_1 = CFG_ROOT + "esr.header.line1"; //$NON-NLS-1$
	public static final String CFG_ESR_HEADER_2 = CFG_ROOT + "esr.header.line2"; //$NON-NLS-1$

	public static final String CFG_PRINT_DIRECT = CFG_ROOT + "print.direct"; //$NON-NLS-1$

	public static final String CFG_PRINT_PRINTER = CFG_ROOT + "print.printer"; //$NON-NLS-1$
	public static final String CFG_PRINT_TRAY = CFG_ROOT + "print.tray"; //$NON-NLS-1$

	public static final String CFG_ESR_PRINT_PRINTER = CFG_ROOT + "esr.print.printer"; //$NON-NLS-1$
	public static final String CFG_ESR_PRINT_TRAY = CFG_ROOT + "esr.print.tray"; //$NON-NLS-1$

	public static final String CFG_PRINT_COMMAND = CFG_ROOT + "print.command"; //$NON-NLS-1$
	public static final String CFG_PRINT_USE_SCRIPT = CFG_ROOT + "print.usescript"; //$NON-NLS-1$

	protected static final String CFG_MAIL_CPY = "mail.copy"; //$NON-NLS-1$
	protected static final String CFG_MAIL_MANDANT_ACCOUNT = "mail.mandant.account"; //$NON-NLS-1$

	public static final String CFG_MSGTEXT_TP_M0 = CFG_ROOT + "pdf.txt.tp"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TG_M0 = CFG_ROOT + "pdf.txt.tg"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TP_M1 = CFG_ROOT + "pdf.txt.M1tp"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TP_M2 = CFG_ROOT + "pdf.txt.M2tp"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TP_M3 = CFG_ROOT + "pdf.txt.M3tp"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TG_M1 = CFG_ROOT + "pdf.txt.M1tg"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TG_M2 = CFG_ROOT + "pdf.txt.M2tg"; //$NON-NLS-1$
	public static final String CFG_MSGTEXT_TG_M3 = CFG_ROOT + "pdf.txt.M3tg"; //$NON-NLS-1$

	private QR_SettingsControl qrs;
	private Manager pdfManager = new Manager();
	String mailPref;
	Mailer mailer;

	private boolean modifyInvoiceState;
	private boolean noPrint;

	private boolean pdfOnly;
	private RnOutputDialog rnOutputDialog;
	private Button buttonOpen;
	private IConfigService cfg = ConfigServiceHolder.get();

	@Override
	public String getDescription() {
		return "Ungrad Drucker";
	}

	public QrRnOutputter() {
		cfg = ConfigServiceHolder.get();
		mailPref = cfg.get(PreferenceConstants.BY_MAIL_IF_CASEVAR, "");
		mailer = new Mailer();
		mailer.showSuccess(false);
	}

	private String[] shouldMail(Rechnung bill) {
		Fall fall = bill.getFall();
		String mailaddr = "";
		String mailbody = cfg.get(PreferenceConstants.BY_MAIL_BODY, "");
		if (!StringTool.isNothing(mailPref)) {
			mailaddr = fall.getInfoString(mailPref);
			if (!StringTool.isMailAddress(mailaddr)) {
				return null;
			}
			String altBody = fall.getInfoString("Mailtext");
			if (!StringTool.isNothing(altBody)) {
				mailbody = altBody;
			}
			return new String[] { mailaddr, mailbody };
		}
		return null;
	}

	@Override
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn, Properties props) {

		if (!props.isEmpty()) {
			initSelectedFromProperties(props);
		} else {
			modifyInvoiceState = true;
			noPrint = false;
		}

		if (StringUtils.isEmpty(OutputterUtil.getXmlOutputDir(CFG_ROOT))) {
			String msg = "Es ist kein XML Ausgabe-Verzeichnis konfiguriert.\nBitte konfigurieren Sie dieses in den Einstellungen.";
			SWTHelper.showError("Fehler beim Rechnungsdruck", msg);
			return new Result<Rechnung>(Result.SEVERITY.ERROR, 2, msg, null, true);
		}
		if (!OutputterUtil.isPdfOutputDirValid(CFG_ROOT)) {
			String msg = "Es ist kein PDF Ausgabe-Verzeichnis konfiguriert.\nBitte konfigurieren Sie dieses in den Einstellungen.";
			SWTHelper.showError("Fehler beim Rechnungsdruck", msg);
			return new Result<Rechnung>(Result.SEVERITY.ERROR, 2, msg, null, true);
		}

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		final Result<Rechnung> res = new Result<Rechnung>();
		final File rsc = new File(PlatformHelper.getBasePath(PLUGIN_ID), "rsc"); //$NON-NLS-1$
		try {
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) {
					monitor.beginTask("Exportiere Rechnungen...", rnn.size() * 10);
					int errors = 0;
					for (Rechnung rn : rnn) {
						IInvoice invoice = CoreModelServiceHolder.get().load(rn.getId(), IInvoice.class).orElseThrow(
								() -> new IllegalStateException("Could not load invoice [" + rn.getId() + "]"));

						XMLExporter ex = new XMLExporter();
						Document dRn = ex.doExport(rn, null, type, true);
						dRn = TarmedXmlUtil.setPrintAtIntermediate(dRn, false);
						monitor.worked(1);
						if (invoice.getState() == InvoiceState.DEFECTIVE) {
							errors++;
							continue;
						}
						String fname = OutputterUtil.getXmlOutputDir(CFG_ROOT) + File.separator + invoice.getNumber()
								+ ".xml"; //$NON-NLS-1$
						try {
							OutputStream fout = VirtualFilesystemServiceHolder.get().of(fname).openOutputStream();
							OutputStreamWriter cout = new OutputStreamWriter(fout, "UTF-8"); //$NON-NLS-1$
							XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
							xout.output(dRn, cout);
							cout.close();
							fout.close();
							// create an new generator for the bill
							ElexisPDFGenerator epdf = new ElexisPDFGenerator(fname, invoice.getNumber(),
									invoice.getState());
							if (pdfOnly || noPrint) {
								epdf.setPrint(false);
							}
							// consider fallback to non QR bill, always fall back for tarmed xml version
							// lower 4.5
							EsrType outputEsrType = ex.getEsrTypeOrFallback(invoice);
							if ("5.0".equals(epdf.getBillVersion())) {
								epdf.printQrBill(rsc);
							} else if ("4.5".equals(epdf.getBillVersion()) && outputEsrType != EsrType.esr9) { //$NON-NLS-1$
								epdf.printQrBill(rsc);
							} else {
								LoggerFactory.getLogger(getClass()).warn("Fallback to ESR9 for xml version [" //$NON-NLS-1$
										+ epdf.getBillVersion() + "] and esrType [" + outputEsrType + "]"); //$NON-NLS-1$ //$NON-NLS-2$
								epdf.printBill(rsc);
							}
							if (modifyInvoiceState) {
								int status_vorher = invoice.getState().numericValue();
								if ((status_vorher == InvoiceState.OPEN.numericValue())
										|| (status_vorher == InvoiceState.DEMAND_NOTE_1.numericValue())
										|| (status_vorher == InvoiceState.DEMAND_NOTE_2.numericValue())
										|| (status_vorher == InvoiceState.DEMAND_NOTE_3.numericValue())) {
									invoice.setState(InvoiceState.fromState(status_vorher + 1));
								}
								invoice.addTrace(InvoiceConstants.OUTPUT, getDescription() + ": " //$NON-NLS-1$
										+ invoice.getState().getLocaleText());
								CoreModelServiceHolder.get().save(invoice);
							}
							List<File> printed = epdf.getPrintedBill();
							if (epdf.isCopy()) {
								for (File pdfFile : printed) {
									PdfUtil.addCopyWatermark(pdfFile);
								}
							}
							String[] mailing = shouldMail(rn);
							if (mailing != null) {
								printed.stream().forEach(f -> {
									if (!f.exists()) {
										throw new IllegalStateException(
												"File to mail does not exist: " + f.getAbsolutePath());
									}
								});
								Map<String, PersistentObject> replacer = new HashMap<>();
								replacer.put("Adressat", rn.getFall().getInvoiceRecipient());
								replacer.put("Mandant", rn.getFall().getRechnungssteller());
								replacer.put("Patient", rn.getFall().getPatient());
								replacer.put("Rechnung", rn);
								Resolver resolver = new Resolver(replacer, true);

								String subject = resolver
										.resolve(cfg.get(PreferenceConstants.BY_MAIL_SUBJECT, "Rechnung"));
								String body = resolver.resolve(mailing[1]);
								mailer.defaultMail(mailing[0], subject, body,
										new String[] { printed.get(0).getAbsolutePath(),
												printed.get(1).getAbsolutePath(), printed.get(2).getAbsolutePath() });
								rn.addTrace(Rechnung.OUTPUT, "by Mail an " + mailing[0]);
								try {
									TimeUnit.MILLISECONDS.sleep(100);
								} catch (InterruptedException e) {

								}
							} else {
								for (File pdfFile : printed) {
									if (pdfFile.exists()) {
										pdfManager.printFromPDF(pdfFile, qrs.selectedPrinter);
										// Program.launch(pdfFile.getAbsolutePath());
									}
								}
							}
						} catch (IllegalStateException e) {
							ExHandler.handle(e);
							SWTHelper.showError("Fehler beim Rechnungsdruck",
									"Bei der Ausgabe ist folgender Fehler aufgetreten.\n\n" + e.getMessage());
							invoice.reject(REJECTCODE.INTERNAL_ERROR, "write error: " + fname); //$NON-NLS-1$
							CoreModelServiceHolder.get().save(invoice);
							continue;
						} catch (Exception e1) {
							ExHandler.handle(e1);
							SWTHelper.showError("Fehler beim Rechnungsdruck",
									"Konnte Datei " + fname + " nicht schreiben");
							invoice.reject(REJECTCODE.INTERNAL_ERROR, "write error: " + fname); //$NON-NLS-1$
							CoreModelServiceHolder.get().save(invoice);
							continue;
						}
						monitor.worked(1);
					}
					pdfOnly = false;
					monitor.done();
					if (errors > 0) {
						SWTHelper.alert("Fehler bei der Übermittlung", Integer.toString(errors)
								+ " Rechnungen waren fehlerhaft. Sie können diese unter Rechnungen mit dem Status fehlerhaft aufsuchen und korrigieren");
					} else {
						SWTHelper.showInfo("Übermittlung beendet", "Es sind keine Fehler aufgetreten");
					}

				}
			}, null);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(Result.SEVERITY.ERROR, 2, ex.getMessage(), null, true);
			Display.getDefault().syncExec(() -> {
				ErrorDialog.openError(null, "Fehler bei der Ausgabe", "Konnte Rechnungsdruck nicht starten",
						ResultAdapter.getResultAsStatus(res));
			});
		}
		return res;
	}

	private void initSelectedFromProperties(Properties props) {
		LoggerFactory.getLogger(getClass()).warn("Initializing with properties " + props.toString()); //$NON-NLS-1$
		modifyInvoiceState = true;
		if (props.get(IRnOutputter.PROP_OUTPUT_MODIFY_INVOICESTATE) instanceof String) {
			String value = (String) props.get(IRnOutputter.PROP_OUTPUT_MODIFY_INVOICESTATE);
			modifyInvoiceState = Boolean.parseBoolean(value);
		}
		if (props.get(IRnOutputter.PROP_OUTPUT_WITH_ESR) instanceof String) {
			String value = (String) props.get(IRnOutputter.PROP_OUTPUT_WITH_ESR);
			LocalConfigService.set(CFG_ROOT + OutputterUtil.CFG_PRINT_BESR, Boolean.parseBoolean(value));
		}
		if (props.get(IRnOutputter.PROP_OUTPUT_WITH_RECLAIM) instanceof String) {
			String value = (String) props.get(IRnOutputter.PROP_OUTPUT_WITH_RECLAIM);
			LocalConfigService.set(CFG_ROOT + OutputterUtil.CFG_PRINT_RF, Boolean.parseBoolean(value));
		}
		if (props.get(IRnOutputter.PROP_OUTPUT_WITH_MAIL) instanceof String) {
			String value = (String) props.get(IRnOutputter.PROP_OUTPUT_WITH_MAIL);
			LocalConfigService.set(CFG_ROOT + CFG_MAIL_CPY, Boolean.parseBoolean(value));
		}
		if (props.get(IRnOutputter.PROP_OUTPUT_NOPRINT) instanceof String) {
			String value = (String) props.get(IRnOutputter.PROP_OUTPUT_NOPRINT);
			noPrint = Boolean.parseBoolean(value);
		}
	}

	private Kontakt getGuarantor(Rechnung rn) {
		ICoverage coverage = CoreModelServiceHolder.get().load(rn.getFall().getId(), ICoverage.class).orElse(null);
		if (coverage != null) {
			IPatient patient = CoreModelServiceHolder.get().load(rn.getFall().getPatient().getId(), IPatient.class)
					.orElse(null);
			if (patient != null) {
				IContact ret = XMLExporterUtil.getGuarantor(XMLExporter.TIERS_PAYANT, patient, coverage);
				if (ret != null) {
					return Kontakt.load(ret.getId());
				}
			}
		}
		return null;
	}

	private boolean shouldSendCopyMail(Rechnung rn) {
		Kontakt guarantor = getGuarantor(rn);
		if (guarantor != null) {
			Fall fall = rn.getFall();
			return !fall.getInvoiceRecipient().equals(guarantor);
		}
		return false;
	}

	private String sendAsMail(Kontakt receiver, Rechnung rechnung, List<File> printed) {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		try {
			String attachmentsString = getAttachmentsString(printed);
			Command sendMailCommand = commandService.getCommand("ch.elexis.core.mail.ui.sendMailNoUi"); //$NON-NLS-1$

			HashMap<String, String> params = new HashMap<String, String>();
			String accountid = ConfigServiceHolder.getGlobal(QrRnOutputter.CFG_ROOT
					+ QrRnOutputter.CFG_MAIL_MANDANT_ACCOUNT + "/" + rechnung.getMandant().getId(), null); //$NON-NLS-1$
			if (accountid != null) {
				params.put("ch.elexis.core.mail.ui.sendMailNoUi.accountid", accountid); //$NON-NLS-1$
			}
			params.put("ch.elexis.core.mail.ui.sendMailNoUi.mandant", rechnung.getMandant().getId()); //$NON-NLS-1$
			params.put("ch.elexis.core.mail.ui.sendMailNoUi.to", receiver.getMailAddress()); //$NON-NLS-1$
			params.put("ch.elexis.core.mail.ui.sendMailNoUi.attachments", attachmentsString); //$NON-NLS-1$
			params.put("ch.elexis.core.mail.ui.sendMailNoUi.subject", "Rechnungskopie vom " + rechnung.getDatumRn()); //$NON-NLS-1$
			params.put("ch.elexis.core.mail.ui.sendMailNoUi.text", //$NON-NLS-1$
					"Anbei finden Sie eine Kopie der Rechnung vom " + rechnung.getDatumRn()
							+ " für Ihre Unterlagen.\n\nBeste Grüsse\n" + rechnung.getMandant().get(Person.TITLE)
							+ StringUtils.SPACE + rechnung.getMandant().getVorname() + StringUtils.SPACE
							+ rechnung.getMandant().getName());

			ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(sendMailCommand, params);
			return (String) PlatformUI.getWorkbench().getService(IHandlerService.class)
					.executeCommand(parametrizedCommmand, null);
		} catch (Exception me) {
			throw new RuntimeException("ch.elexis.core.mail.ui.sendMailNoUi not found", me); //$NON-NLS-1$
		}
	}

	private String getAttachmentsString(List<File> attachments) {
		StringBuilder sb = new StringBuilder();
		for (File file : attachments) {
			if (sb.length() > 0) {
				sb.append(":::"); //$NON-NLS-1$
			}
			sb.append(file.getAbsolutePath());
		}
		return sb.toString();
	}

	@Override
	public boolean canStorno(Rechnung rn) {
		return false;
	}

	@Override
	public boolean canBill(Fall fall) {
		return true;
	}

	@Override
	public Object createSettingsControl(Object parent) {
		qrs = new QR_SettingsControl((Composite) parent);
		return qrs;
	}

	@Override
	public void customizeDialog(Object dialog) {
		if (dialog instanceof RnOutputDialog) {
			rnOutputDialog = (RnOutputDialog) dialog;
			rnOutputDialog.setOkButtonText(Messages.Core_Print);
			buttonOpen = rnOutputDialog.addCustomButton(Messages.Core_Open);
			RowData rowData = new RowData();
			rowData.width = 91;
			rowData.height = 26;
			buttonOpen.setLayoutData(rowData);
			buttonOpen.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					pdfOnly = true;
					rnOutputDialog.customButtonPressed(IDialogConstants.OK_ID);
				}
			});
			rnOutputDialog.redrawLayout();
		}
	}

	@Override
	public void saveComposite() {
		qrs.doSave();
		LocalConfigService.set(CFG_ROOT + OutputterUtil.CFG_PRINT_BESR, qrs.cbQRPage.getSelection());
		LocalConfigService.set(CFG_ROOT + OutputterUtil.CFG_PRINT_RF, qrs.cbTarmedForm.getSelection());
		if (!OutputterUtil.useGlobalOutputDirs()) {
			LocalConfigService.set(CFG_ROOT + XMLDIR, qrs.tOutdirXML.getText());
			LocalConfigService.set(CFG_ROOT + PDFDIR, qrs.tOutdirPDF.getText());
		}
		LocalConfigService.flush();
	}

	@Override
	public void openOutput(IInvoice invoice, LocalDateTime timestamp, InvoiceState invoiceState) {
		try {
			File esrFile = VirtualFilesystemServiceHolder.get().of(OutputterUtil.getPdfOutputDir(QrRnOutputter.CFG_ROOT)
					+ File.separator + invoice.getNumber() + "_esr.pdf").toFile().orElse(null);
			File rfFile = VirtualFilesystemServiceHolder.get().of(OutputterUtil.getPdfOutputDir(QrRnOutputter.CFG_ROOT)
					+ File.separator + invoice.getNumber() + "_rf.pdf").toFile().orElse(null);
			File qrFile = VirtualFilesystemServiceHolder.get().of(OutputterUtil.getPdfOutputDir(QrRnOutputter.CFG_ROOT)
					+ File.separator + invoice.getNumber() + "_qr.pdf").toFile().orElse(null);
			if (esrFile.exists()) {
				Program.launch(esrFile.getAbsolutePath());
			} else {
				LoggerFactory.getLogger(getClass()).info("File [" + esrFile.getAbsolutePath() + "] does not exist"); //$NON-NLS-1$
			}
			if (rfFile.exists()) {
				Program.launch(rfFile.getAbsolutePath());
			} else {
				LoggerFactory.getLogger(getClass()).info("File [" + rfFile.getAbsolutePath() + "] does not exist"); //$NON-NLS-1$
			}
			if (qrFile.exists()) {
				Program.launch(qrFile.getAbsolutePath());
			} else {
				LoggerFactory.getLogger(getClass()).info("File [" + qrFile.getAbsolutePath() + "] does not exist"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error opening output", e);
		}
	}

}