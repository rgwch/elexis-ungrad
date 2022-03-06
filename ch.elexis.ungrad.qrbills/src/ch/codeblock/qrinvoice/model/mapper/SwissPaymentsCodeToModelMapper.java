/*-
 * #%L
 * QR Invoice Solutions
 * %%
 * Copyright (C) 2017 - 2022 Codeblock GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * -
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * -
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * -
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses are available for this software. These replace the above
 * AGPLv3 terms and offer support, maintenance and allow the use in commercial /
 * proprietary products.
 * -
 * More information on commercial licenses are available at the following page:
 * https://www.qr-invoice.ch/licenses/
 * #L%
 */
package ch.codeblock.qrinvoice.model.mapper;

import ch.codeblock.qrinvoice.model.*;
import ch.codeblock.qrinvoice.util.DecimalFormatFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Currency;

public class SwissPaymentsCodeToModelMapper {
    public static SwissPaymentsCodeToModelMapper create() {
        return new SwissPaymentsCodeToModelMapper();
    }

    public QrInvoice map(SwissPaymentsCode spc) {
        QrInvoice qrInvoice = new QrInvoice();

        qrInvoice.setHeader(mapHeader(spc));
        qrInvoice.setCreditorInformation(mapCreditorInformation(spc));
        qrInvoice.setUltimateCreditor(mapUltimateCreditor(spc));
        qrInvoice.setPaymentAmountInformation(mapPaymentAmountInformation(spc));
        qrInvoice.setUltimateDebtor(mapUltimateDebtor(spc));
        qrInvoice.setPaymentReference(mapPaymentReference(spc));
        qrInvoice.setAlternativeSchemes(mapAlternativeSchemes(spc));

        return qrInvoice;
    }


    private UltimateDebtor mapUltimateDebtor(final SwissPaymentsCode spc) {
        final UltimateDebtor ultimateDebtor = new UltimateDebtor();
        final AddressType addressType = AddressType.parse(spc.getUdAdrTp());
        ultimateDebtor.setAddressType(addressType);
        ultimateDebtor.setName(spc.getUdName());
        ultimateDebtor.setCountry(spc.getUdCtry());

        if (addressType != null) {
            switch (addressType) {
                case STRUCTURED:
                    ultimateDebtor.setStreetName(spc.getUdStrtNmOrAdrLine1());
                    ultimateDebtor.setHouseNumber(spc.getUdBldgNbOrAdrLine2());
                    ultimateDebtor.setPostalCode(spc.getUdPstCd());
                    ultimateDebtor.setCity(spc.getUdTwnNm());
                    break;
                case COMBINED:
                    ultimateDebtor.setAddressLine1(spc.getUdStrtNmOrAdrLine1());
                    ultimateDebtor.setAddressLine2(spc.getUdBldgNbOrAdrLine2());
                    break;
            }
        }

        if (ultimateDebtor.isNotEmpty()) {
            return ultimateDebtor;
        } else {
            return null;
        }
    }

    private Header mapHeader(final SwissPaymentsCode spc) {
        final Header header = new Header();
        header.setQrType(spc.getQrType());
        try {
            header.setVersion(Short.parseShort(spc.getVersion()));
        } catch (NumberFormatException nfe) {
            throw new ParseException("Unable to parse Swiss QR Code version. Value was '" + spc.getVersion() + "'");
        }
        try {
            header.setCodingType(Byte.parseByte(spc.getCoding()));
        } catch (NumberFormatException nfe) {
            throw new ParseException("Unable to parse Swiss QR Code coding. Value was '" + spc.getCoding() + "'");
        }
        return header;
    }

    private CreditorInformation mapCreditorInformation(final SwissPaymentsCode spc) {
        final CreditorInformation creditorInformation = new CreditorInformation();
        creditorInformation.setIban(spc.getIban());
        creditorInformation.setCreditor(mapCreditor(spc));
        return creditorInformation;
    }

    private Creditor mapCreditor(final SwissPaymentsCode spc) {
        final Creditor creditor = new Creditor();
        final AddressType addressType = AddressType.parse(spc.getCrAdrTp());
        creditor.setAddressType(addressType);
        creditor.setName(spc.getCrName());
        creditor.setCountry(spc.getCrCtry());

        if (addressType != null) {
            switch (addressType) {
                case STRUCTURED:
                    creditor.setStreetName(spc.getCrStrtNmOrAdrLine1());
                    creditor.setHouseNumber(spc.getCrBldgNbOrAdrLine2());
                    creditor.setPostalCode(spc.getCrPstCd());
                    creditor.setCity(spc.getCrTwnNm());
                    break;
                case COMBINED:
                    creditor.setAddressLine1(spc.getCrStrtNmOrAdrLine1());
                    creditor.setAddressLine2(spc.getCrBldgNbOrAdrLine2());
                    break;
            }
        }

        return creditor;
    }

    private UltimateCreditor mapUltimateCreditor(final SwissPaymentsCode spc) {
        final UltimateCreditor ultimateCreditor = new UltimateCreditor();
        final AddressType addressType = AddressType.parse(spc.getUcrAdrTp());
        ultimateCreditor.setAddressType(addressType);
        ultimateCreditor.setName(spc.getUcrName());
        ultimateCreditor.setCountry(spc.getUcrCtry());

        if (addressType != null) {
            switch (addressType) {
                case STRUCTURED:
                    ultimateCreditor.setStreetName(spc.getUcrStrtNmOrAdrLine1());
                    ultimateCreditor.setHouseNumber(spc.getUcrBldgNbOrAdrLine2());
                    ultimateCreditor.setPostalCode(spc.getUcrPstCd());
                    ultimateCreditor.setCity(spc.getUcrTwnNm());
                    break;
                case COMBINED:
                    ultimateCreditor.setAddressLine1(spc.getUcrStrtNmOrAdrLine1());
                    ultimateCreditor.setAddressLine2(spc.getUcrBldgNbOrAdrLine2());
                    break;
            }
        }

        if (ultimateCreditor.isNotEmpty()) {
            return ultimateCreditor;
        } else {
            return null;
        }
    }

    private PaymentReference mapPaymentReference(final SwissPaymentsCode spc) {
        final PaymentReference paymentReference = new PaymentReference();
        if (spc.getTp() != null) {
            paymentReference.setReferenceType(ReferenceType.parse(spc.getTp()));
        } else {
            paymentReference.setReferenceType(null);
        }
        paymentReference.setReference(spc.getRef());

        paymentReference.setAdditionalInformation(mapAdditionalInformation(spc));
        return paymentReference;
    }

    private AdditionalInformation mapAdditionalInformation(final SwissPaymentsCode spc) {
        final AdditionalInformation additionalInformation = new AdditionalInformation();
        additionalInformation.setUnstructuredMessage(spc.getUstrd());
        additionalInformation.setTrailer(spc.getTrailer());
        additionalInformation.setBillInformation(spc.getStrdBkgInf());

        return additionalInformation;
    }

    private PaymentAmountInformation mapPaymentAmountInformation(final SwissPaymentsCode spc) {
        final PaymentAmountInformation paymentAmountInformation = new PaymentAmountInformation();
        final String amt = spc.getAmt();
        if (amt != null) {
            final DecimalFormat decimalFormat = DecimalFormatFactory.createSwissPaymentsCodeAmountFormat();
            decimalFormat.setParseBigDecimal(true);
            paymentAmountInformation.setAmount((BigDecimal) decimalFormat.parseObject(amt, new ParsePosition(0)));
        } else {
            paymentAmountInformation.setAmount(null);
        }

        if (spc.getCcy() != null) {
            paymentAmountInformation.setCurrency(Currency.getInstance(spc.getCcy()));
        } else {
            paymentAmountInformation.setCurrency(null);
        }

        return paymentAmountInformation;
    }


    private AlternativeSchemes mapAlternativeSchemes(final SwissPaymentsCode spc) {
        final AlternativeSchemes alternativeSchemes = new AlternativeSchemes();
        alternativeSchemes.setAlternativeSchemeParameters(spc.getAltPmts());
        return alternativeSchemes;
    }

}
