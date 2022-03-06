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
package ch.codeblock.qrinvoice.model;

import ch.codeblock.qrinvoice.paymentpartreceipt.LayoutDefinitions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>QR-bill «DO NOT USE FOR PAYMENT» (Source: <a href="https://www.paymentstandards.ch/dam/downloads/notification-en.pdf">https://www.paymentstandards.ch/dam/downloads/notification-en.pdf</a>).</p>
 * <p>This document is primarily addressed to invoice issuers and recipients who use QR-bills
 * not only for payment transactions, but also for notifications. For such notifications to be
 * correctly handled, this also pertains to financial institutions, network partners, software
 * partners and other service providers in the Swiss payment traffic</p>
 */
public class DoNotUseForPayment {
    /**
     * For notifications amount has to be 0.00 (not empty).<br>
     * From the specification:
     * <ul>
     * <li>The amount field may not be left blank. A blank amount field is only used if the amount to be paid is selected by the debtor themselves. Hence, this pertains to another use case.</li>
     * <li>The amount of CHF 0.00 guarantees that in case of conversion into eBill (alternative procedure), the invoice is converted into a notification that may not be released for payment.</li>
     * </ul>
     */
    public static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;
    /**
     * QR Invoice internal label key
     */
    public static final String LABEL_KEY = "DoNotUseForPayment";

    /**
     * The valid unstructured messages in all supported languages.
     */
    public static final Set<String> UNSTRUCTURED_MESSAGES = Collections.unmodifiableSet(
            LayoutDefinitions.SUPPORTED_LOCALES.stream()
                    .map(DoNotUseForPayment::getUnstructuredMessage)
                    .collect(Collectors.toSet())
    );

    /**
     * @param locale
     * @return The unstructured message in the corresponding language according to the specification
     */
    public static String getUnstructuredMessage(final Locale locale) {
        return ResourceBundle.getBundle("qrinvoice", locale).getString(LABEL_KEY).trim();
    }

    /**
     * This method translates a notification unstructured message of any language into the desired one for final payment part &amp; receipt output. 
     * For any other string, no translation is done, original string is simply returned.
     *
     * @param unstructuredMessage The passed unstructured message, e.g. "DO NOT USE FOR PAYMENT"
     * @param locale              The language the notification string is to be localized. e.g. GERMAN
     * @return The localized notification string, e.g. "NICHT ZUR ZAHLUNG VERWENDEN" or the originally passed unstructured message string
     */
    public static String localize(final String unstructuredMessage, final Locale locale) {
        if (unstructuredMessage == null) {
            return null;
        }

        if (UNSTRUCTURED_MESSAGES.contains(unstructuredMessage)) {
            return getUnstructuredMessage(locale);
        } else {
            return unstructuredMessage;
        }
    }
}
