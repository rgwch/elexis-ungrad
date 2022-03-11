/*******************************************************************************
 * Copyright (c) 2018 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/
package ch.elexis.ungrad.qrbills;

import java.util.List;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.rgw.crypt.BadParameterException;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrCode.Ecc;
import io.nayuki.qrcodegen.QrSegment;

public class QR_Generator {

	/* ISO 20022 Defaults */
	/* See https://www.paymentstandards.ch/dam/downloads/ig-qr-bill-de.pdf */
	/* https://www.paymentstandards.ch/de/shared/communication-grid.html */
	/* https://www.paymentstandards.ch/dam/downloads/style-guide-de.pdf */
	private static final QrCode.Ecc ecc = Ecc.MEDIUM;
	private static final int VERSION = 25;
	// private static final int size = 117; // total size must be 46x46 mm
	// private static final double module_size = 0.4; // module size at least 0.4 mm
	private static final int quiet_zone = 5; // quiet zone around QR 5mm.
	private static final int border = 10;
	private static final int logo_size = 7; // swiss symbol 7x7 mm.
	private static final String ENC = "latin-1"; // Character encoding

	/*
	 * private static final String[] QR_Elements = { "QRType", // Always SPC,
	 * Mandatory // Do not print "Version", // Always 0200 this time, Mandatory //
	 * Do not print "Coding", // Always 1, Mandatory // Do not print "rIBAN", //
	 * receiver's IBAN, Mandatory, 21 chars, Must start with CH or LI "rName", //
	 * receiver's name, Mandatory, max 70 chars "rStrtNm", // street or PO Box,
	 * Optional, max 70 Chars "rBldgNmb", // Building number, Optional, max 16 Chars
	 * "rPstCd", // ZIP Code, Mandatory, max 16 Chars "rTwnNm", // Place, Mandatory,
	 * max 35 Chars "rCtry", // Country, 2 Chars as in ISO 3166-1 "urName", //
	 * ultimate receiver name, Optional, max 70 Chars "urStrtNm", // ultimate
	 * receiver's street or PO Box name, Optional, max 70 Chars "urBldgNmb", //
	 * ultimate receiver's building number, Optional, max 16 Chars "urPStCd", //
	 * ultimate receiver's ZIP Code, depending urName, max 16 Chars "urTwnNm", //
	 * ultimate receiver's Place, depending urName, max 35 Chars "urCtry", //
	 * ultimate receiver's Country, depending urName, 2 Chars "Amt", // Amount,
	 * decimal, 2 digits, Optional, max 12 chars, no leading zeros, decimalpoint "."
	 * "Ccy", // Currency, Must be CHF or EUR "ReqdExctnDt", // Date due as
	 * yyyy-mm-dd "dbtName", // Debtor Name, max 70 Chars "dbtStrtNm", // Debor
	 * Street, max 70 Chars "dbtBldg", // debtor Building Nr., max 16 Chars
	 * "dbtPstCd", // Debtor postal code, max 16 Chars "dbtTwnNm", // Debtor place,
	 * max 35 Chars "dbtCtry", // Debtor Country, 2 Chars "Tp", // Payment reference
	 * type, must be QRR,SCOR or NON. "Ref", // Payment reference (26 numbers, plus
	 * 27th. is modulo-10) "Ustrd" // unstructured message, max 140 chars
	 * 
	 * };
	 */

	/**
	 * Generate an iso 20022 conformant QR-Code (which is, in fact, an iso 18004
	 * conformant QR-Code with some restrictions, some predefinitioons and a swiss
	 * cross in the center.
	 * 
	 * @param rn the bill to take the data from
	 * @return an SVG Image with the QR Code.
	 */
	public String generate(Rechnung rn, BillDetails bill) throws BadParameterException {
		if (bill.qrIBAN.length() != 27) {
			throw new BadParameterException("Bad QR-Reference", 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("SPC\r\n").append("0200\r\n").append("1\r\n").append(bill.IBAN).append(StringConstants.CRLF);

		addKontaktS(bill.biller, sb);
		addKontaktK(null, sb);
		appendOptional(sb, checkSize(bill.amount.getAmountAsString(), 12));
		appendOptional(sb, checkSize(bill.currency, 3));
		// TimeTool now = new TimeTool();
		// now.addDays(30);
		// appendOptional(sb, now.toString(TimeTool.DATE_MYSQL));
		addKontaktK(bill.patient, sb);
		sb.append("QRR").append(StringConstants.CRLF);
		sb.append(bill.qrIBAN).append(StringConstants.CRLF);

		// sb.append(checkSize( Integer.toString(bill.numCons) + " Konsultationen von "
		// + bill.firstDate + " bis " + bill.lastDate,140));
		sb.append("EPD").append(StringConstants.CRLF);
		// List<QrSegment> segments = QrSegment.makeSegments(sb.toString());
		// QrCode result = QrCode.encodeSegments(segments, ecc, 20, VERSION, -1, false);
		QrCode result = QrCode.encodeBinary(sb.toString().getBytes(), ecc);
		return toSvgString(result);
	}

	/*
	 * Add kontkat data ISO 20022 Addresstype K
	 */
	private void addKontaktK(Kontakt k, StringBuilder sb) throws BadParameterException {
		if (k == null) {
			for (int i = 0; i < 7; i++) {
				sb.append(StringConstants.CRLF);
			}
		} else {
			sb.append("K").append(StringConstants.CRLF);
			String name = k.get("Bezeichnung1") + " " + k.get("Bezeichnung2");
			sb.append(checkSize(name, 70)).append(StringConstants.CRLF);
			String straddr = k.get(Kontakt.FLD_STREET);
			if (StringTool.isNothing(straddr)) {
				sb.append(StringConstants.CRLF);
			} else {
				sb.append(checkSize(straddr, 70)).append(StringConstants.CRLF);
			}
			String place = k.get(Kontakt.FLD_ZIP) + " " + k.get(Kontakt.FLD_PLACE);
			if (StringTool.isNothing(place)) {
				sb.append(StringConstants.CRLF);
			} else {
				sb.append(checkSize(place, 70)).append(StringConstants.CRLF);
			}
			sb.append(StringConstants.CRLF).append(StringConstants.CRLF);
			String cntry = k.get(Kontakt.FLD_COUNTRY);
			if (StringTool.isNothing(cntry)) {
				cntry = "CH";
			}
			sb.append(checkSize(cntry, 2)).append(StringConstants.CRLF);
		}
	}

	/*
	 * Add Kontakt data ISO 20022 conformant AddressType S (separate street name and
	 * buding Nr). This is a quick hack that works only, if the building number
	 * starts with a digit.
	 */
	private void addKontaktS(Kontakt k, StringBuilder sb) throws BadParameterException {
		if (k == null) {
			for (int i = 0; i < 7; i++) {
				sb.append(StringConstants.CRLF);
			}

		} else {
			sb.append("S").append(StringConstants.CRLF);
			String name = k.get("Bezeichnung1") + " " + k.get("Bezeichnung2");
			sb.append(checkSize(name, 70)).append(StringConstants.CRLF);
			String straddr = k.get(Kontakt.FLD_STREET);
			if (StringTool.isNothing(straddr)) {
				sb.append(StringConstants.CRLF);
				sb.append(StringConstants.CRLF);
			} else {
				String[] strnr = straddr.split(" [0-9]");
				appendOptional(sb, checkSize(strnr[0], 70));
				if (strnr.length > 1) {
					char tr = straddr.charAt(strnr[0].length() + 1);
					strnr[1] = tr + strnr[1];
					appendOptional(sb, checkSize(strnr[1], 16));
				} else {
					sb.append(StringConstants.CRLF);
				}
			}
			appendOptional(sb, checkSize(k.get(Kontakt.FLD_ZIP), 16));
			appendOptional(sb, checkSize(k.get(Kontakt.FLD_PLACE), 35));
			String cntry = k.get(Kontakt.FLD_COUNTRY);
			if (StringTool.isNothing(cntry)) {
				cntry = "CH";
			}
			appendOptional(sb, checkSize(cntry, 2));
		}
	}

	private String checkSize(String in, int max) throws BadParameterException {
		if (in.length() > max) {
			throw new BadParameterException(in + " is too long", 4);
		}
		return in;
	}

	/*
	 * Even if we don't set a field, we must put a CRLF since there's no way to
	 * identify fields otherwise.
	 */
	private void appendOptional(StringBuilder sb, String val) {
		if (!StringTool.isNothing(val)) {
			sb.append(val);
		}
		sb.append(StringConstants.CRLF);
	}

	/*
	 * create an SVG image from a QR Code to insert into the template.
	 * 
	 * @param qr the QRCode to embed
	 * 
	 * @return a String describing an SVG image.
	 */
	private String toSvgString(QrCode qr) {
		StringBuilder sb = new StringBuilder();
		int dim = qr.size + border * 2;
		double center = dim / 2.0;
		double module_size = 46.0 / qr.size;
		double ratio = logo_size / module_size;
		double cross = center - ratio / 2;
		sb.append(String.format("<svg width=\"66mm\" height=\"66mm\" viewBox=\"0 0 %1$d %1$d\">\n", dim));
		sb.append("<rect width=\"100%\" height=\"100%\" fill=\"#FFFFFF\"/>\n");
		sb.append("<path d=\"");
		boolean head = true;
		for (int y = -border; y < qr.size + border; y++) {
			for (int x = -border; x < qr.size + border; x++) {
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
		sb.append("<g transform=\"translate(" + cross + "," + cross + ")\">");
		sb.append(createCross(module_size));
		sb.append("</g></svg>\n");
		return sb.toString();
	}

	/*
	 * Swiss cross: Field size should be 7x7 mm Cross must be 2/3 of field length of
	 * crossbars must be Width+1/6 Width
	 */
	private String createCross(double module_size) {
		double quadrat = logo_size / module_size; // total field
		double bar_length = quadrat * 2 / 3; // Length of bars
		double offset1 = (quadrat - bar_length) / 2; // offset to near edge
		double bar_width = (3.0 * bar_length) / 10.0; // width of bars
		double offset2 = (quadrat - bar_width) / 2; // offset to far edge
		StringBuilder ret = new StringBuilder();
		// black field
		ret.append("<rect width=\"" + quadrat + "\" height=\"" + quadrat + "\" fill=\"black\" />");
		// white border
		ret.append("<rect width=\"" + quadrat + "\" height=\"" + quadrat
				+ "\" fill=\"none\" stroke=\"white\" stroke-width=\"0.8\" />");
		// crossbars
		ret.append("<rect x=\"" + offset2 + "\" y= \"" + offset1 + "\" width=\"" + bar_width + "\" height=\""
				+ bar_length + "\" fill=\"white\"/>");
		ret.append("<rect x=\"" + offset1 + "\" y=\"" + offset2 + "\" width=\"" + bar_length + "\" height=\""
				+ bar_width + "\" fill=\"white\" />");
		return ret.toString();
	}

}
