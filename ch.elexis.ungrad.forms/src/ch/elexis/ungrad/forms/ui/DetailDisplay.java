package ch.elexis.ungrad.forms.ui;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.ungrad.forms.model.Controller;
import ch.elexis.ungrad.forms.model.Template;

public class DetailDisplay extends Composite {

	ScrolledForm form;
	Composite inlay;
	Template template;
	
	public DetailDisplay(Composite parent, Controller controller) {
		super(parent, SWT.NONE);
		setLayoutData(SWTHelper.getFillGridData());
		setLayout(new FillLayout());
		FormToolkit tk=new FormToolkit(getDisplay());
		form=tk.createScrolledForm(this);
		Composite body=form.getBody();
		body.setLayout(new GridLayout());
		inlay=new Composite(body,SWT.NONE);
		inlay.setLayoutData(SWTHelper.getFillGridData());
		body.setBackground(new Color(getDisplay(),100,100,100));
		inlay.setBackground(new Color(getDisplay(),200,200,200));
		inlay.setLayout(new GridLayout());
	}

	void show(Template template) {
		this.template=template;
		form.setText(template.getTitle());
		for(Control c:inlay.getChildren()) {
			c.dispose();
		}
	
		for(Entry<String, String>e:template.getInputs().entrySet()) {
			Label label=new Label(inlay,SWT.NONE);
			label.setText(e.getKey());
			label.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text text=new Text(inlay,SWT.MULTI|SWT.BORDER);
			text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.setText(e.getValue().replace("<br />", "X"));
		}
		inlay.layout();
	}
	
	public void output() {
		
	}
}
