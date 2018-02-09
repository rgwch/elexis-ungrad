package ch.elexis.ungrad.qrbills;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.ungrad.Resolver;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

/**
 * An Elexis-IRnOutputter for ISO 20022 conformant bills. Embeds a QR Code in a HTML template and writes the result in a html
 * file. (To be converted to pdf or to print directly, whatever). 
 * @author gerry
 *
 */
public class QR_Outputter implements IRnOutputter {
	String outputDir;
	Map<String, IPersistentObject> replacer = new HashMap<>();
	
	public QR_Outputter(){}
	
	@Override
	public String getDescription(){
		return "Rechnung mit QR Code";
	}
	
	@Override
	public boolean canStorno(Rechnung rn){
		return false;
	}
	
	@Override
	public boolean canBill(Fall fall){
		return true;
	}
	
	@Override
	public Control createSettingsControl(final Object parent){
		final Composite parentInc = (Composite) parent;
		Composite ret = new Composite(parentInc, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		Label l = new Label(ret, SWT.NONE);
		l.setText(Messages.XMLExporter_PleaseEnterOutputDirectoryForBills);
		l.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		final Text text = new Text(ret, SWT.READ_ONLY | SWT.BORDER);
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Button b = new Button(ret, SWT.PUSH);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				outputDir = new DirectoryDialog(parentInc.getShell(), SWT.OPEN).open();
				CoreHub.localCfg.set(PreferenceConstants.RNN_DIR, outputDir);
				text.setText(outputDir);
			}
		});
		b.setText(Messages.XMLExporter_Change);
		outputDir = CoreHub.localCfg.get(PreferenceConstants.RNN_DIR,
			CorePreferenceInitializer.getDefaultDBPath());
		text.setText(outputDir);
		return ret;
	}
	
	@Override
	public void saveComposite(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn, Properties props){
		Result<Rechnung> res = new Result<Rechnung>();
		QR_Generator qr = new QR_Generator();
		String template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator
			+ "rsc" + File.separator + "qrbill_template_page1.html";
		File templateFile = new File(template);
		try {
			String rawHTML = FileTool.readTextFile(templateFile);
			
			for (Rechnung rn : rnn) {
				try {
					BillDetails bill=new BillDetails(rn);
					replacer.put("Adressat", bill.adressat);
					Resolver resolver = new Resolver(replacer, true);
					
					String cookedHTML = resolver.resolve(rawHTML);
					String svg = qr.generate(rn,bill);
					String finished = cookedHTML.replace("[QRCODE]", svg)
							.replace("[CURRENCY]", bill.currency)
							.replace("[AMOUNT]", bill.amount.getAmountAsString())
							.replace("[IBAN]", bill.IBAN)
							.replace("[BILLER]", bill.biller_address)
							.replace("[INFO]", Integer.toString(bill.numCons)+" Konsultationen")
							.replace("[ADDRESSEE]", bill.addressee)
							.replace("[DUE]", bill.dateDue);
					
							
					FileTool.writeTextFile(new File(outputDir, rn.getRnId() + ".html"), finished);
					res.add(new Result<Rechnung>(rn));
				} catch (Exception ex) {
					ExHandler.handle(ex);
					res.add(new Result<Rechnung>(SEVERITY.ERROR, 2, ex.getMessage(), rn, true));
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			res.add(new Result<Rechnung>(SEVERITY.ERROR, 1,
				"Could  not find templateFile " + template, null, true));
		}
		return res;
	}
	
}
