package ch.elexis.ungrad.common.ui;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;

public class ExtIdPreferences extends PreferencePage implements IWorkbenchPreferencePage {

	Kontakt current;
	Composite fields;
	ScrolledComposite scroller;
	Map<Object, Object> extInfo;
	private IContextService contextService=ContextServiceHolder.get();
	
	public ExtIdPreferences() {
		// TODO Auto-generated constructor stub
	}

	public ExtIdPreferences(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public ExtIdPreferences(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	protected Control createContents(Composite parent) {
		IMandator mandant=contextService.getActiveMandator().get();
		current=Kontakt.load(mandant.getId());
		parent.setLayout(new GridLayout());
		Hyperlink contact = new Hyperlink(parent, SWT.NONE);
		contact.setText(current.getLabel());
		contact.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				KontaktSelektor ksl = new KontaktSelektor(getShell(), Kontakt.class, "Kontakt auswählen",
						"Bitte wählen Sie den Kontat, dessen Extinfo bearbeitet werden soll", new String[] {});
				int ret = ksl.open();
				if (ret == Window.OK) {
					Kontakt k = (Kontakt) ksl.getSelection();
					contact.setText(k.getLabel());
					current = k;
					setContents();
				}
			};
		});
		contact.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		scroller = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scroller.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		fields = new Composite(scroller, SWT.NONE);
		fields.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		scroller.setContent(fields);
		// fields.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		fields.setLayout(new GridLayout(4, false));
		setContents();
		return parent;
	}

	private void setContents() {
		for (Control ch : fields.getChildren()) {
			ch.dispose();
		}
		if (current != null && current.isAvailable()) {
			extInfo = current.getMap(PersistentObject.FLD_EXTINFO);
			for (Entry<Object, Object> elem : extInfo.entrySet()) {
				Object ov = elem.getValue();
				Object ok = elem.getKey();
				if (ov instanceof String && ok instanceof String) {
					Button btn = new Button(fields, SWT.NONE);
					btn.setImage(Images.IMG_DELETE.getImage());
					Label l = new Label(fields, SWT.NONE);
					l.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					l.setText((String) ok);
					Text t = new Text(fields, SWT.BORDER);
					t.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					t.setText((String) ov);
					Button bOk = new Button(fields, SWT.NONE);
					bOk.setImage(Images.IMG_CHECKBOX.getImage());
					bOk.setEnabled(false);
					bOk.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseUp(MouseEvent e) {
							extInfo.put(elem.getKey(), t.getText());
							bOk.setEnabled(false);
						}

					});
					t.addModifyListener(new ModifyListener() {

						@Override
						public void modifyText(ModifyEvent e) {
							bOk.setEnabled(true);

						}
					});
					btn.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseUp(MouseEvent e) {
							extInfo.remove(elem.getKey());
							bOk.dispose();
							t.dispose();
							l.dispose();
							btn.dispose();
							scroller.layout();
						}

					});

				}
			}
			Button btn = new Button(fields, SWT.NONE);
			btn.setImage(Images.IMG_DELETE.getImage());
			
			Text l = new Text(fields, SWT.BORDER);
			l.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Text t = new Text(fields, SWT.BORDER);
			t.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Button bOk = new Button(fields, SWT.NONE);
			bOk.setImage(Images.IMG_OK.getImage());
			btn.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					l.setText("");
					t.setText("");
					bOk.setEnabled(false);
				}

			});
			t.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					bOk.setEnabled(true);

				}
			});
			bOk.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseUp(MouseEvent e) {
					extInfo.put(l.getText(), t.getText());
					bOk.setEnabled(false);
				}

			});
		
			
		}
		fields.setSize(fields.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scroller.layout();
	}

	@Override
	protected void performApply() {
		current.setMap(PersistentObject.FLD_EXTINFO, extInfo);
		super.performApply();
	}

	@Override
	public boolean okToLeave() {
		performApply();
		return super.okToLeave();
	}

	@Override
	protected void performDefaults() {
		setContents();
		super.performDefaults();
	}

	

}
