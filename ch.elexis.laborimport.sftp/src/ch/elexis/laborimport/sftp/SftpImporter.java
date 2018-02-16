/**
 * (c) 2008-2018 by G. Weirich
 * All rights reserved
 * 
 */
package ch.elexis.laborimport.sftp;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Messages;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.ui.importer.div.importers.DefaultHL7Parser;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public class SftpImporter extends ImporterPage {
	public static final String MY_LAB = CoreHub.localCfg.get(Preferences.SFTP_LABNAME, "<unbekannt>");
	public static final String PLUGIN_ID = "ch.elexis.laborimport.sftp";

	private static final int FILE = 1;
	private static final int DIRECT = 2;

	private HL7Parser hlp = new DefaultHL7Parser(MY_LAB);
	Shell parentShell;

	public SftpImporter() {
	}

	@Override
	public Composite createPage(final Composite parent) {

		parentShell = parent.getShell();
		LabImporter labImporter = new LabImporter(parent, this);
		labImporter.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return labImporter;
	}

	/**
	 * Connct to the sftp-site and fetch all files provided there.
	 * 
	 * @return a ch.rgw.tools.Result object containing the Results
	 */
	private Result<?> importDirect() {
		Result<?> result = new Result<String>("OK");

		String downloadDirPath = CoreHub.localCfg.get(Preferences.DL_DIR, CoreHub.getTempDir().toString());
		String pwd = CoreHub.localCfg.get(Preferences.SFTP_PWD, null);
		String host = CoreHub.localCfg.get(Preferences.SFTP_URL, null);
		String user = CoreHub.localCfg.get(Preferences.SFTP_USER, null);
		File downloadDir = new File(downloadDirPath);
		File archiveDir = new File(downloadDir, "archive");
		if (!archiveDir.exists()) {
			archiveDir.mkdir();
		}

		int res = -1;
		if (pwd != null && user != null && host != null) {
			try {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						try {
							JSch jsch = new JSch();
							int port = CoreHub.localCfg.get(Preferences.SFTP_PORT, 22);
							Session session = jsch.getSession(user, host, port);

							// username and password will be given via UserInfo
							// interface.
							UserInfo ui = new JschUserInfo();
							session.setUserInfo(ui);

							session.connect();

							Channel channel = session.openChannel("sftp");
							channel.connect();
							ChannelSftp c = (ChannelSftp) channel;
							log.log("SFTP Version " + c.version(), Log.INFOS);
							java.io.InputStream in = System.in;
							java.io.PrintStream out = System.out;
							java.util.Vector<LsEntry> vv = c.ls(".");
							int count = 0;
							int err = 0;
							if (vv != null) {
								for (LsEntry entry : vv) {
									String fname = entry.getFilename();
									if (fname.matches("\\.\\.?")) {
										continue;
									}
									if (!fname.matches("[a-zA-Z0-9_\\-\\.\\/]+")) {
										result.add(new Result(SEVERITY.WARNING, 2,
												"Dateiname " + fname + " enthält ungültige Zeichen", null, true));
										err++;
										continue;
									}
									log.log(entry.getLongname(), Log.INFOS);
									File localFile = new File(downloadDir, entry.getFilename());
									Result r;
									try {
										c.get(entry.getFilename(), localFile.getAbsolutePath());
										r = hlp.importFile(localFile, archiveDir, false);
									} catch (Exception ex) {
										ExHandler.handle(ex);
										err++;
										r = new Result<String>(SEVERITY.WARNING, 1,
												"Fehler beim Import von " + entry.getFilename(),
												localFile.getAbsolutePath(), true);
									}

									result.add(r);
									if (r.isOK()) {
										if (CoreHub.localCfg.get(Preferences.SFTP_DELETE, true)) {
											c.rm(entry.getFilename());
										}
										count++;
									} else {
										err++;
									}
								}
							}
							c.quit();
							session.disconnect();
							SWTHelper.showInfo("Import beendet", "Es wurden " + count + " Dateien importiert.\nBei "
									+ err + " Dateien sind Fehler aufgetreten.");
							if (!result.isOK()) {
								ResultAdapter.displayResult(result, "Fehlermeldungen: ");
							}
						} catch (SftpException sfex) {
							SWTHelper.showError("SFTP Fehler", sfex.getMessage());
						} catch (JSchException jshex) {
							SWTHelper.showError("Jsch Fehler", jshex.getMessage());

						}
					}

				});

			} catch (Throwable e) {
				// ExHandler.handle(e);
				SWTHelper.showError("Fehler bei Badena Import", "Es ist ein Fehler aufgetreten", e.getMessage());
			}
		}
		return result;
	}

	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception {
		int type;
		try {
			String sType = results[0];
			type = Integer.parseInt(sType);
		} catch (NumberFormatException ex) {
			type = FILE;
		}

		if ((type != FILE) && (type != DIRECT)) {
			type = FILE;
		}

		if (type == FILE) {
			String filename = results[1];
			return ResultAdapter.getResultAsStatus(hlp.importFile(filename, false));
		} else {
			return ResultAdapter.getResultAsStatus(importDirect());
		}
	}

	@Override
	public String getDescription() {
		return "Bitte wählen Sie eine Datei im HL7-Format oder die Direktübertragung zum Import aus";
	}

	@Override
	public String getTitle() {
		return "Labor " + MY_LAB;
	}

	/**
	 * An importer that lets the user select a file to import or directly import
	 * the data from the lab. The chosen type (file or direct import) is stored
	 * in results[0] (FILE or DIRECT). If FILE is chosen, the file path is
	 * stored in results[1].
	 * 
	 * @author gerry, danlutz
	 * 
	 */
	private class LabImporter extends Composite {
		private final Button bFile;
		private final Button bDirect;
		private final Button bBrowse;

		private final Text tFilename;

		public LabImporter(final Composite parent, final ImporterPage home) {
			super(parent, SWT.BORDER);
			setLayout(new GridLayout(3, false));

			bFile = new Button(this, SWT.RADIO);
			bFile.setText("Import aus Datei (HL7)");
			bFile.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

			Label lFile = new Label(this, SWT.NONE);
			lFile.setText("    " + Messages.ImporterPage_file); //$NON-NLS-1$ //$NON-NLS-2$
			GridData gd = SWTHelper.getFillGridData(1, false, 1, false);
			gd.horizontalAlignment = GridData.END;
			gd.widthHint = lFile.getSize().x + 20;

			tFilename = new Text(this, SWT.BORDER);
			tFilename.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

			bBrowse = new Button(this, SWT.PUSH);
			bBrowse.setText(Messages.ImporterPage_browse); // $NON-NLS-1$

			bDirect = new Button(this, SWT.RADIO);
			bDirect.setText("Direkter Import");
			bDirect.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

			int type = CoreHub.localCfg.get("ImporterPage/" + home.getTitle() + "/type", FILE); //$NON-NLS-1$ //$NON-NLS-2$

			home.results = new String[2];
			String filename = CoreHub.localCfg.get("ImporterPage/" + home.getTitle() + "/filename", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tFilename.setText(filename);
			home.results[1] = filename;

			if (type == FILE) {
				bFile.setSelection(true);
				bDirect.setSelection(false);
				tFilename.setEnabled(true);
				bBrowse.setEnabled(true);

				home.results[0] = new Integer(FILE).toString();
			} else {
				bFile.setSelection(false);
				bDirect.setSelection(true);
				bBrowse.setEnabled(false);
				tFilename.setEnabled(false);

				home.results[0] = new Integer(DIRECT).toString();
			}

			if (CoreHub.localCfg.get(Preferences.SFTP_PWD, null) == null) {
				bDirect.setEnabled(false);
			}

			SelectionAdapter sa = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button button = (Button) e.getSource();
					if (!button.getSelection()) {
						return;
					}

					int type = FILE;

					if (button == bFile) {
						type = FILE;
						CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/type", FILE); //$NON-NLS-1$ //$NON-NLS-2$
						bBrowse.setEnabled(true);
						tFilename.setEnabled(true);
						bFile.setSelection(true);
					} else if (button == bDirect) {
						type = DIRECT;
						CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/type", DIRECT); //$NON-NLS-1$ //$NON-NLS-2$
						bBrowse.setEnabled(false);
						bDirect.setSelection(true);
						tFilename.setEnabled(false);
					}
				}
			};

			bFile.addSelectionListener(sa);
			bDirect.addSelectionListener(sa);

			bBrowse.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					bFile.setSelection(true);
					bDirect.setSelection(false);

					FileDialog fdl = new FileDialog(parent.getShell(), SWT.OPEN);
					fdl.setFilterExtensions(new String[] { "*" }); //$NON-NLS-1$
					fdl.setFilterNames(new String[] { Messages.ImporterPage_allFiles }); // $NON-NLS-1$
					String filename = fdl.open();
					if (filename == null) {
						filename = "";
					}

					tFilename.setText(filename);
					home.results[0] = new Integer(FILE).toString();
					home.results[1] = filename;
					CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/type", FILE); //$NON-NLS-1$ //$NON-NLS-2$
					CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/filename", filename); //$NON-NLS-1$ //$NON-NLS-2$
				}

			});
		}
	}
}
