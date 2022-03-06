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
package ch.codeblock.qrinvoice.paymentpartreceipt;

import ch.codeblock.qrinvoice.model.AdditionalInformation;
import ch.codeblock.qrinvoice.model.Address;
import ch.codeblock.qrinvoice.model.PaymentReference;
import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.util.AddressUtils;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.codeblock.qrinvoice.model.ReferenceType.WITHOUT_REFERENCE;

public class PaymentPartReceiptLayoutHelper {
    public static PaymentPartReceiptLayoutHelper create() {
        return new PaymentPartReceiptLayoutHelper();
    }

    private final Logger logger = LoggerFactory.getLogger(PaymentPartReceiptLayoutHelper.class);

    public PaymentPartReceiptLayout chooseLayout(final QrInvoice qrInvoice) {
        final int layoutPoints = calculateLayoutPoints(qrInvoice);

        final PaymentPartReceiptLayout layout;
        if (layoutPoints >= 19) {
            layout = PaymentPartReceiptLayout.COMPRESSED;
        } else if (layoutPoints >= 17) {
            layout = PaymentPartReceiptLayout.NARROWED;
        } else {
            layout = PaymentPartReceiptLayout.RELAXED;
        }

        logger.debug("LayoutPoints={} Layout={}", layoutPoints, layout);
        return layout;
    }

    private int calculateLayoutPoints(final QrInvoice qrInvoice) {
        int points = 0;
        points += calculateLayoutPoints(qrInvoice.getCreditorInformation().getCreditor());
        points += calculateLayoutPoints(qrInvoice.getUltimateDebtor());

        // if no debtor is provided, free text field is written which takes a lot of space
        points += (AddressUtils.isEmpty(qrInvoice.getUltimateDebtor())) ? 7 : 0;

        final PaymentReference paymentReference = qrInvoice.getPaymentReference();
        final AdditionalInformation additionalInformation = paymentReference.getAdditionalInformation();
        points += calculateLayoutPoints(50, additionalInformation.getUnstructuredMessage());
        points += calculateLayoutPoints(50, additionalInformation.getBillInformation());

        // 2 points if reference is given
        points += WITHOUT_REFERENCE.equals(paymentReference.getReferenceType()) ? 0 : 2;

        return points;
    }

    private int calculateLayoutPoints(final Address address) {
        if (AddressUtils.isEmpty(address)) {
            return 0;
        } else {
            int result = 1;
            result += calculateLayoutPoints(35, address.getName());
            // either structured
            result += calculateLayoutPoints(35, address.getStreetName(), address.getHouseNumber());
            result += calculateLayoutPoints(35, address.getPostalCode(), address.getCity());
            // or combined
            result += calculateLayoutPoints(35, address.getAddressLine1());
            result += calculateLayoutPoints(35, address.getAddressLine2());
            return result;
        }
    }

    private int calculateLayoutPoints(final int threshold, final String... str) {
        final int sum = StringUtils.length(str);
        if (sum == 0) {
            return 0;
        } else if (sum < threshold) {
            return 1;
        } else {
            return 2;
        }
    }

    public static boolean compressedLayout(final PaymentPartReceiptLayout layout) {
        return layout == PaymentPartReceiptLayout.COMPRESSED;
    }

    public static boolean uncompressedLayout(final PaymentPartReceiptLayout layout) {
        return !compressedLayout(layout);
    }
}
