package ch.elexis.ungrad.qrbills;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Properties;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.ungrad.Resolver;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public class QR_Outputter implements IRnOutputter {

	
	public QR_Outputter(){
		// TODO Auto-generated constructor stub
	}
	
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
	public Object createSettingsControl(Object parent){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void saveComposite(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn, Properties props){
		Result<Rechnung> res = new Result<Rechnung>();
		Resolver resolver=new Resolver();
		QR_Generator qr = new QR_Generator();
		String template = PlatformHelper.getBasePath("ch.elexis.ungrad.qrbills") + File.separator
			+ "rsc" + File.separator + "qrbill_template_page1.html";
		File templateFile = new File(template);
		try {
			String rawHTML = FileTool.readTextFile(templateFile);
			
			for (Rechnung rn : rnn) {
				try {
					Fall fall=rn.getFall();
					Patient patient=fall.getPatient();
					Kontakt biller=rn.getMandant().getRechnungssteller();
					String cookedHTML=resolver.resolve(rawHTML);
					String svg = qr.generate(rn);
					String finished=cookedHTML.replaceAll("QRCODE", svg);
					FileTool.writeTextFile(new File("/home/gerry/test.html"), finished);
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
