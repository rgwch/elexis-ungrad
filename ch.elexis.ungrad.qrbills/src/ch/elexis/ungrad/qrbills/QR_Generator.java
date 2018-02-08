package ch.elexis.ungrad.qrbills;

import java.awt.image.BufferedImage;
import java.util.List;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrCode.Ecc;
import io.nayuki.qrcodegen.QrSegment;

public class QR_Generator {
	
	/* ISO 20022 Defaults */
	private static final QrCode.Ecc ecc = Ecc.MEDIUM;
	private static final int VERSION = 25;
	private static final int size = 46; // total size must be 46x46 mm
	private static final double min_module_size = 0.4; // module size at least 0.4 mm
	private static final int quiet_zone = 5; // quiet zone around QR 5mm.
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
	
	public BufferedImage generate(Rechnung rn){
		String iban = "CH13001223455666"; // todo
		Kontakt rnSteller = rn.getMandant().getRechnungssteller();
		StringBuilder sb = new StringBuilder();
		sb.append("SPC\r\n").append("0100\r\n").append("1\r\n").append(iban)
			.append(StringConstants.CRLF)
			.append(rnSteller.getLabel()).append(StringConstants.CRLF);
		
		List<QrSegment> segments=QrSegment.makeSegments(sb.toString());
		QrCode result=QrCode.encodeSegments(segments, ecc);	
		return result.toImage(1, 10);
	}
}
