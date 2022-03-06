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
package ch.codeblock.qrinvoice.util;

import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.paymentpartreceipt.LayoutDefinitions;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalFormatFactory {
    private DecimalFormatFactory() {
    }

    public static DecimalFormat createSwissPaymentsCodeAmountFormat() {
        // 4.3.3 The amount element is to be entered without leading zeroes, including decimal separators and two decimal places.
        // Decimal, maximum 12-digits permitted, including decimal separators. Only decimal points (".") are permitted as decimal separators.

        final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        formatSymbols.setDecimalSeparator(SwissPaymentsCode.AMOUNT_DECIMAL_FORMAT_DECIMAL_SEPARATOR);

        final DecimalFormat decimalFormat = new DecimalFormat(SwissPaymentsCode.AMOUNT_DECIMAL_FORMAT, formatSymbols);
        decimalFormat.setMinimumFractionDigits(2);
        return decimalFormat;
    }

    public static DecimalFormat createPrintAmountFormat() {
        final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        formatSymbols.setDecimalSeparator(LayoutDefinitions.AMOUNT_DECIMAL_FORMAT_DECIMAL_SEPARATOR);
        formatSymbols.setGroupingSeparator(LayoutDefinitions.AMOUNT_DECIMAL_FORMAT_GROUPING_SEPARATOR);

        final DecimalFormat decimalFormat = new DecimalFormat(LayoutDefinitions.AMOUNT_DECIMAL_FORMAT_PRINT, formatSymbols);
        decimalFormat.setMinimumFractionDigits(2); // 3.5.3 The amount must always be printed with two decimal places
        return decimalFormat;
    }

}
