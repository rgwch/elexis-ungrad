package ch.elexis.ungrad.lucinda.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;

public class ChangeNameDialog extends Dialog {
	String inputText;
	Text input;

	protected ChangeNameDialog(Shell parentShell, String name) {
		super(parentShell);
		inputText = name;

	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			inputText = input.getText();
		}
		super.buttonPressed(buttonId);
	}

	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		input = new Text(parent, SWT.BORDER);
		input.setLayoutData(SWTHelper.getFillGridData());
		input.setText(inputText);
		return input;
	}

}
