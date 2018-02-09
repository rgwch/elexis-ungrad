package ch.elexis.ungrad.qrbills;

import java.util.List;

import ch.elexis.TarmedRechnung.Messages;
import ch.elexis.TarmedRechnung.TarmedACL;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
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
	
	/**
	 * Generate an iso 20022 conformant QR-Code (which is, in fact, an iso 18004 conformant QR-Code with
	 * some restrictions, some predefinitioons and a swiss cross in the center.
	 * @param rn the bill to take the data from
	 * @return an SVG Image with the QR Code.
	 */
	public String generate(Rechnung rn, BillDetails bill){
		StringBuilder sb = new StringBuilder();
		sb.append("SPC\r\n").append("0100\r\n").append("1\r\n").append(bill.IBAN)
			.append(StringConstants.CRLF);
		
		addKontakt(bill.bank, sb);
		addKontakt(bill.biller, sb);
		appendOptional(sb, bill.amount.getAmountAsString());
		appendOptional(sb, bill.currency);
		TimeTool now = new TimeTool();
		now.addDays(30);
		appendOptional(sb, now.toString(TimeTool.DATE_MYSQL));
		addKontakt(bill.patient, sb);
		sb.append("QRR").append(StringConstants.CRLF);
		sb.append(bill.qrIBAN).append(StringConstants.CRLF);
		sb.append("Memo");
		QrCode result;
		List<QrSegment> segments = QrSegment.makeSegments(sb.toString());
		result = QrCode.encodeSegments(segments, ecc, VERSION, VERSION, -1, false);
		return toSvgString(result);
	}
	
	/*
	 * Add Kontakt data ISO 20022 conformant (separate street name and buding Nr). This is a quick hack that works only, if the building number
	 * starts with a digit.
	 */
	private void addKontakt(Kontakt k, StringBuilder sb){
		sb.append(k.getLabel()).append(StringConstants.CRLF);
		String straddr = k.get(Kontakt.FLD_STREET);
		if (StringTool.isNothing(straddr)) {
			sb.append(StringConstants.CRLF);
			sb.append(StringConstants.CRLF);
		} else {
			String[] strnr = straddr.split(" [0-9]");
			appendOptional(sb, strnr[0]);
			if (strnr.length > 1) {
				appendOptional(sb, strnr[1]);
			} else {
				sb.append(StringConstants.CRLF);
			}
		}
		appendOptional(sb, k.get(Kontakt.FLD_ZIP));
		appendOptional(sb, k.get(Kontakt.FLD_PLACE));
		String cntry = k.get(Kontakt.FLD_COUNTRY);
		if (StringTool.isNothing(cntry)) {
			cntry = "CH";
		}
		appendOptional(sb, cntry);
		
	}
	
	/*
	 * Even if we don't set a field, we must put a CRLF since there's no way to identify fields otherwise.
	 */
	private void appendOptional(StringBuilder sb, String val){
		if (!StringTool.isNothing(val)) {
			sb.append(val);
		}
		sb.append(StringConstants.CRLF);
	}
	
	/*
	 * create an SVG image from a QR Code to insert into the template. 
	 * @param qr the QRCode to embed
	 * @return a String describing an SVG image.
	 */
	private String toSvgString(QrCode qr){
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<svg width=\"57mm\" height=\"57mm\" viewBox=\"0 0 %1$d %1$d\">\n",
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
