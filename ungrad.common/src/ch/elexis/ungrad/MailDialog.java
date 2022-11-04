package ch.elexis.ungrad;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class MailDialog extends TitleAreaDialog {

	Text tSender;
	Text tSubject;
	Text tBody;
	String mailTo;

	public String sender = "";
	public String subject = "";
	public String body = "";

	public MailDialog(Shell parentShell, String mailTo) {
		super(parentShell);
		this.mailTo = mailTo;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData());
		ret.setLayout(new GridLayout(2, false));
		Label lSender = new Label(ret, SWT.NONE);
		lSender.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		lSender.setText("Absender");
		tSender = new Text(ret, SWT.BORDER);
		tSender.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tSender.setText(sender);

		Label lSubject = new Label(ret, SWT.NONE);
		lSubject.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		lSubject.setText("Betreff");
		tSubject = new Text(ret, SWT.BORDER);
		tSubject.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tSubject.setText(subject);

		tBody = new Text(ret, SWT.BORDER | SWT.MULTI);
		tBody.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		String esc=body.replace("<br />", "\n");
		tBody.setText(esc);

		return ret;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Dokument als Mailanhang versenden");
		if (StringTool.isNothing(mailTo)) {
			setErrorMessage("Es ist kein gültiger Addressat gesetzt");
		}
	}

	@Override
	protected void okPressed() {
		sender = tSender.getText();
		subject = tSubject.getText();
		body = tBody.getText().replace("\n", "<br />");
		super.okPressed();
	}

}
