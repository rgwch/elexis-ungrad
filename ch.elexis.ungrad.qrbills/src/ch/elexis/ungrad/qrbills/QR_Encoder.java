package ch.elexis.ungrad.qrbills;

import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.builder.QrInvoiceBuilder;
import ch.codeblock.qrinvoice.model.validation.*;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import ch.codeblock.qrinvoice.FontFamily;
import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.QrInvoiceCodeCreator;
import ch.codeblock.qrinvoice.QrInvoicePaymentPartReceiptCreator;
import ch.codeblock.qrinvoice.output.PaymentPartReceipt;
import ch.codeblock.qrinvoice.output.QrCode;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Rechnung;
import ch.rgw.crypt.BadParameterException;

public class QR_Encoder {
	public String generate(Rechnung rn, BillDetails bill) throws BadParameterException, UnsupportedEncodingException {
		final QrInvoice qr = QrInvoiceBuilder.create().creditorIBAN(bill.IBAN)
				.paymentAmountInformation(p -> p.chf(bill.amount.getAmount()))
				.creditor(c -> c.combinedAddress()
						.name(bill.biller.get(Kontakt.FLD_NAME1) + " " + bill.biller.get(Kontakt.FLD_NAME2))
						.addressLine1(bill.biller.get(Kontakt.FLD_STREET))
						.addressLine2(bill.biller.get(Kontakt.FLD_ZIP) + " " + bill.biller.get(Kontakt.FLD_PLACE))
						.country("CH"))
				.paymentReference(r -> r.qrReference(bill.qrIBAN)).build();
		/*
		final QrCode qrCode = QrInvoiceCodeCreator.create()
				.qrInvoice(qr)
				.outputFormat(OutputFormat.SVG)
				.desiredQrCodeSize(500)
				.createQrCode();
		final byte[] image = qrCode.getData();
		*/
		final PaymentPartReceipt ppr=QrInvoicePaymentPartReceiptCreator.create()
				.qrInvoice(qr)
				.outputFormat(OutputFormat.PDF)
				.fontFamily(FontFamily.LIBERATION_SANS)
				.locale(Locale.GERMAN)
				.createPaymentPartReceipt();
		//return new String(image,"utf-8");
		final byte[] image=ppr.getData();
		return new String(image,"utf-8");
	}
}
