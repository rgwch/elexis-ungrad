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

import ch.codeblock.qrinvoice.MappingException;
import ch.codeblock.qrinvoice.model.*;
import ch.codeblock.qrinvoice.util.DecimalFormatFactory;
import ch.codeblock.qrinvoice.util.StringUtils;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModelToSwissPaymentsCodeMapper {

    public static ModelToSwissPaymentsCodeMapper create() {
        return new ModelToSwissPaymentsCodeMapper();
    }

    public SwissPaymentsCode map(QrInvoice qrInvoice) throws MappingException {
        final SwissPaymentsCode spc = new SwissPaymentsCode();

        mapHeader(qrInvoice.getHeader(), spc);
        mapCreditorInformation(qrInvoice.getCreditorInformation(), spc);
        mapUltimateCreditor(qrInvoice.getUltimateCreditor(), spc);
        mapPaymentAmountInformation(qrInvoice.getPaymentAmountInformation(), spc);
        mapUltimateDebtor(qrInvoice.getUltimateDebtor(), spc);
        mapPaymentReference(qrInvoice.getPaymentReference(), spc);
        mapAlternativeSchemes(qrInvoice.getAlternativeSchemes(), spc);

        return spc;
    }

    private void mapPaymentReference(final PaymentReference paymentReference, final SwissPaymentsCode spc) {
        if (paymentReference == null) {
            throw new MappingException("PaymentReference must be given");
        }
        spc.setTp(paymentReference.getReferenceType().getReferenceTypeCode());
        spc.setRef(paymentReference.getReference());
        mapAdditionalInformation(paymentReference.getAdditionalInformation(), spc);

    }

    void mapAdditionalInformation(final AdditionalInformation additionalInformation, final SwissPaymentsCode spc) {
        spc.setUstrd(additionalInformation.getUnstructuredMessage());
        spc.setTrailer(additionalInformation.getTrailer());

        if (StringUtils.isNotEmpty(additionalInformation.getBillInformation()) && additionalInformation.getBillInformationObject() != null) {
            if (!additionalInformation.getBillInformation().equals(additionalInformation.getBillInformationObject().toBillInformationString())) {
                throw new MappingException("Both BillInformation string and BillInformationObject were set but were inconsistent");
            } else {
                spc.setStrdBkgInf(additionalInformation.getBillInformation());
            }
        } else if (StringUtils.isNotEmpty(additionalInformation.getBillInformation())) {
            spc.setStrdBkgInf(additionalInformation.getBillInformation());
        } else if (additionalInformation.getBillInformationObject() != null) {
            spc.setStrdBkgInf(additionalInformation.getBillInformationObject().toBillInformationString());
        }
    }

    private void mapPaymentAmountInformation(final PaymentAmountInformation paymentAmountInformation, final SwissPaymentsCode spc) {
        if (paymentAmountInformation == null) {
            throw new MappingException("PaymentAmountInformation must be given");
        }

        final BigDecimal amount = paymentAmountInformation.getAmount();
        if (amount != null) {
            spc.setAmt(DecimalFormatFactory.createSwissPaymentsCodeAmountFormat().format(amount));
        } else {
            spc.setAmt(null);
        }

        final Currency currency = paymentAmountInformation.getCurrency();
        if (currency != null) {
            spc.setCcy(currency.getCurrencyCode());
        } else {
            spc.setCcy(null);
        }
    }

    private void mapUltimateDebtor(final UltimateDebtor ultimateDebtor, final SwissPaymentsCode spc) {
        if (ultimateDebtor == null) {
            // optional
            return;
        }

        final AddressType addressType = ultimateDebtor.getAddressType();
        spc.setUdAdrTp(mapAddressType(addressType));
        spc.setUdName(ultimateDebtor.getName());
        spc.setUdCtry(ultimateDebtor.getCountry());

        if (addressType != null) {
            switch (addressType) {
                case STRUCTURED:
                    spc.setUdStrtNmOrAdrLine1(ultimateDebtor.getStreetName());
                    spc.setUdBldgNbOrAdrLine2(ultimateDebtor.getHouseNumber());
                    spc.setUdPstCd(ultimateDebtor.getPostalCode());
                    spc.setUdTwnNm(ultimateDebtor.getCity());
                    break;
                case COMBINED:
                    spc.setUdStrtNmOrAdrLine1(ultimateDebtor.getAddressLine1());
                    spc.setUdBldgNbOrAdrLine2(ultimateDebtor.getAddressLine2());
                    break;
            }
        }
    }

    private void mapHeader(final Header header, final SwissPaymentsCode spc) {
        if (header == null) {
            throw new MappingException("Header must be given");
        }
        spc.setQrType(header.getQrType());
        spc.setVersion(String.format("%04d", header.getVersion()));
        spc.setCoding(Byte.toString(header.getCodingType())); // fixed length 1
    }

    private void mapCreditorInformation(final CreditorInformation creditorInformation, final SwissPaymentsCode spc) {
        if (creditorInformation == null) {
            throw new MappingException("CreditorInformation must be given");
        }
        spc.setIban(creditorInformation.getIban());
        mapCreditor(creditorInformation.getCreditor(), spc);
    }

    private void mapCreditor(final Creditor creditor, final SwissPaymentsCode spc) {
        if (creditor == null) {
            throw new MappingException("Creditor must be given");
        }
        final AddressType addressType = creditor.getAddressType();
        spc.setCrAdrTp(mapAddressType(addressType));
        spc.setCrName(creditor.getName());
        spc.setCrCtry(creditor.getCountry());

        if (addressType != null) {
            switch (addressType) {
                case STRUCTURED:
                    spc.setCrStrtNmOrAdrLine1(creditor.getStreetName());
                    spc.setCrBldgNbOrAdrLine2(creditor.getHouseNumber());
                    spc.setCrPstCd(creditor.getPostalCode());
                    spc.setCrTwnNm(creditor.getCity());
                    break;
                case COMBINED:
                    spc.setCrStrtNmOrAdrLine1(creditor.getAddressLine1());
                    spc.setCrBldgNbOrAdrLine2(creditor.getAddressLine2());
                    break;
            }
        }
    }

    private void mapUltimateCreditor(final UltimateCreditor ultimateCreditor, final SwissPaymentsCode spc) {
        if (ultimateCreditor == null) {
            // optional
            return;
        }
        final AddressType addressType = ultimateCreditor.getAddressType();
        spc.setUcrAdrTp(mapAddressType(addressType));
        spc.setUcrName(ultimateCreditor.getName());
        spc.setUcrCtry(ultimateCreditor.getCountry());

        if (addressType != null) {
            switch (addressType) {
                case STRUCTURED:
                    spc.setUcrStrtNmOrAdrLine1(ultimateCreditor.getStreetName());
                    spc.setUcrBldgNbOrAdrLine2(ultimateCreditor.getHouseNumber());
                    spc.setUcrPstCd(ultimateCreditor.getPostalCode());
                    spc.setUcrTwnNm(ultimateCreditor.getCity());
                    break;
                case COMBINED:
                    spc.setUcrStrtNmOrAdrLine1(ultimateCreditor.getAddressLine1());
                    spc.setUcrBldgNbOrAdrLine2(ultimateCreditor.getAddressLine2());
                    break;
            }
        }
    }

    private void mapAlternativeSchemes(final AlternativeSchemes alternativeSchemes, final SwissPaymentsCode spc) {
        if (alternativeSchemes == null || alternativeSchemes.getAlternativeSchemeParameters() == null) {
            // optional
            return;
        }
        spc.setAltPmts(alternativeSchemes.getAlternativeSchemeParameters().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList()));
    }

    private String mapAddressType(final AddressType addressType) {
        if (addressType == null) {
            return null;
        } else {
            return addressType.getAddressTypeCodeAsString();
        }
    }

}
