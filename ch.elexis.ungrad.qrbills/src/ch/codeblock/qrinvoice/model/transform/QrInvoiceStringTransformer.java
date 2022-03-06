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
package ch.codeblock.qrinvoice.model.transform;

import ch.codeblock.qrinvoice.model.*;

import java.util.stream.Collectors;

// TODO test and document class
public class QrInvoiceStringTransformer {
    public static QrInvoiceStringTransformer create(StringTransformerFunction stringTransformerFunction) {
        return new QrInvoiceStringTransformer(stringTransformerFunction);
    }

    private final StringTransformerFunction stringTransformerFunction;

    public QrInvoiceStringTransformer(StringTransformerFunction stringTransformerFunction) {
        this.stringTransformerFunction = stringTransformerFunction;
    }

    public void transform(QrInvoice qrInvoice) {
        address(qrInvoice.getUltimateCreditor()); // currently not in use (not permitted)
        address(qrInvoice.getUltimateDebtor());
        if (qrInvoice.getCreditorInformation() != null) {
            qrInvoice.getCreditorInformation().setIban(stringTransformerFunction.transform(qrInvoice.getCreditorInformation().getIban()));
            address(qrInvoice.getCreditorInformation().getCreditor());
        }
        final PaymentReference paymentReference = qrInvoice.getPaymentReference();
        if (paymentReference != null) {
            paymentReference.setReference(stringTransformerFunction.transform(paymentReference.getReference()));
            final AdditionalInformation additionalInformation = paymentReference.getAdditionalInformation();
            if (additionalInformation != null) {
                additionalInformation.setBillInformation(stringTransformerFunction.transform(additionalInformation.getBillInformation()));
                additionalInformation.setUnstructuredMessage(stringTransformerFunction.transform(additionalInformation.getUnstructuredMessage()));
            }
        }
        final AlternativeSchemes alternativeSchemes = qrInvoice.getAlternativeSchemes();
        if (alternativeSchemes != null && alternativeSchemes.getAlternativeSchemeParameters() != null) {
            alternativeSchemes.setAlternativeSchemeParameters(
                    alternativeSchemes.getAlternativeSchemeParameters().stream().map(stringTransformerFunction::transform).collect(Collectors.toList())
            );
        }
    }


    private void address(Address address) {
        if (address != null) {
            address.setName(stringTransformerFunction.transform(address.getName()));
            address.setName(stringTransformerFunction.transform(address.getStreetName()));
            address.setName(stringTransformerFunction.transform(address.getHouseNumber()));
            address.setName(stringTransformerFunction.transform(address.getPostalCode()));
            address.setName(stringTransformerFunction.transform(address.getCity()));
            address.setName(stringTransformerFunction.transform(address.getAddressLine1()));
            address.setName(stringTransformerFunction.transform(address.getAddressLine2()));
            address.setName(stringTransformerFunction.transform(address.getCountry()));
        }
    }
}
