package ch.elexis.ungrad.qrbills;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.List;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.elexis.tarmedprefs.TarmedRequirements;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrCode.Ecc;
import io.nayuki.qrcodegen.QrSegment;

public class QR_Generator {
	
	/* ISO 20022 Defaults */
	/* See https://www.paymentstandards.ch/dam/downloads/ig-qr-bill-de.pdf */
	private static final QrCode.Ecc ecc = Ecc.MEDIUM;
	private static final int VERSION = 25;
	private static final int size = 117; // total size must be 46x46 mm
	private static final double module_size = 0.4; // module size at least 0.4 mm
	private static final int quiet_zone = 5; // quiet zone around QR 5mm.
	private static final int border = 10;
	private static final int logo_size = 7; // swiss symbol 7x7 mm.
	private static final String ENC = "latin-1"; // Character encoding
	
	private static final String[] QR_Elements = {
		"QRType", // Always SPC, Mandatory
		"Version", // Always 0100 this time, Mandatory
		"Coding", // Always 1, Mandatory
		"rIBAN", // receiver's IBAN, Mandatory
		"rName", // receiver's name, Mandatory
		"rStrtNm", // street or PO Box, Optional
		"rBldgNmb", // Building number, Optional
		"rPstCd", // ZIP Code, Mandatory
		"rTwnNm", // Place, Mandatory
		"rCtry", // Country
		"urName", // ultimate receiver name, Optional
		"urStrtNm", // ultimate receiver's street or PO Box name, Optional
		"urBldgNmb", // ultimate receiver's building number, Optional
		"urPStCd", // ultimate receiver's ZIP Code, depending urName
		"urTwnNm", // ultimate receiver's Place, depending urName
		"urCtry", // ultimate receiver's Country, depending urName
		"Amt", // Amount, decinal, 2 digits, Optional
		"Ccy", // Currency, Must be CHF or EUR
		"ReqdExctnDt", // Date due as yyyy-mm-dd
		"dbtName", // Debtor Name
		"dbtStrtNm", // Debor Street
		"dbtBldg", // debtor Building Nr.
		"dbtPstCd", // Debtor postal code
		"dbtTwnNm", // Debtor place
		"dbtCtry", // Debtor Country
		"Tp", // Payment reference type, must be QRR,SCOR or NON.
		"Ref", // Payment reference (26 numbers, plus 27th. is modulo-10)
		"Ustrd" // unstructured mesage
	
	};
	
	
	public String generate(Rechnung rn){
		String iban = "CH13001223455666"; // todo
		Kontakt rnSteller = rn.getMandant().getRechnungssteller();
		TarmedACL ta=TarmedACL.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("SPC\r\n")
			.append("0100\r\n")
			.append("1\r\n")
			.append(iban).append(StringConstants.CRLF);
		
		String curr =
				(String) rnSteller.getExtInfoStoredObjectByKey(
					Messages.XMLExporter_Currency);
			if (StringTool.isNothing(curr)) {
				curr = "CHF"; //$NON-NLS-1$
			}
		Kontakt bank=Kontakt.load(rnSteller.getInfoString(ta.RNBANK));
		addKontakt(bank, sb);
		addKontakt(rnSteller,sb);	
		appendOptional(sb, rn.getBetrag().getAmountAsString());
		appendOptional(sb, curr);
		TimeTool now=new TimeTool();
		now.addDays(30);
		appendOptional(sb,now.toString(TimeTool.DATE_MYSQL));
		addKontakt(rn.getFall().getPatient(), sb);
		sb.append("QRR").append(StringConstants.CRLF);
		String qriban=StringTool.pad(StringTool.LEFT, '0', StringTool.addModulo10(rn.getNr()), 27);
		sb.append(qriban).append(StringConstants.CRLF);
		sb.append("Memo");
		QrCode result;
		//try {
			List<QrSegment> segments=QrSegment.makeSegments(sb.toString());
			result = QrCode.encodeSegments(segments, ecc, VERSION, VERSION, -1, false);
			return toSvgString(result);
		/*} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}*/
	}
	
	private void addKontakt(Kontakt k, StringBuilder sb) {
		sb.append(k.getLabel()).append(StringConstants.CRLF);
		String straddr=k.get(Kontakt.FLD_STREET);
		if(StringTool.isNothing(straddr)) {
			sb.append(StringConstants.CRLF);
			sb.append(StringConstants.CRLF);
		}else {
			String[] strnr=straddr.split(" [0-9]");
			appendOptional(sb, strnr[0]);
			if(strnr.length>1) {
				appendOptional(sb, strnr[1]);
			}else {
				sb.append(StringConstants.CRLF);
			}				
		}
		appendOptional(sb, k.get(Kontakt.FLD_ZIP));
		appendOptional(sb, k.get(Kontakt.FLD_PLACE));
		String cntry=k.get(Kontakt.FLD_COUNTRY);
		if(StringTool.isNothing(cntry)) {
			cntry="CH";
		}
		appendOptional(sb, cntry);
		
	}
	
	private void appendOptional(StringBuilder sb, String val) {
		if(!StringTool.isNothing(val)) {
			sb.append(val);
		}
		sb.append(StringConstants.CRLF);
	}
	private String toSvgString(QrCode qr){
		StringBuilder sb = new StringBuilder();

		//sb.append("<svg width=\"200\" height=\"200\">");
		sb.append(String.format(
			"<svg width=\"47mm\" height=\"47mm\" viewBox=\"0 0 %1$d %1$d\">\n",
			size + border * 2));
		sb.append("<rect width=\"100%\" height=\"100%\" fill=\"#FFFFFF\"/>\n");
		sb.append("<path d=\"");
		boolean head = true;
		for (int y = -border; y < size + border; y++) {
			for (int x = -border; x < size + border; x++) {
				if (qr.getModule(x, y)) {
					if (head)
						head = false;
					else
						sb.append(" ");
					sb.append(String.format("M%d,%d h1 v1 h-1 z", x + border, y + border));
				}
			}
		}
		sb.append("\" fill=\"#000000\"/>\n");
		sb.append("</svg>\n");
		return sb.toString();
	}
	
}
