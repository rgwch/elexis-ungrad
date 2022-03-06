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
package ch.codeblock.qrinvoice.qrcode;

import ch.codeblock.qrinvoice.QrInvoiceCodeParser;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.model.parser.SwissPaymentsCodeParser;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SwissQrCodeFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QrInvoiceCodeParser.class);

    public static List<String> filterSwissPaymentCodes(final List<String> qrCodeStrings) {
        return qrCodeStrings.stream().filter(SwissQrCodeFilter::filter).collect(Collectors.toList());
    }

    public static boolean filter(final String text) {
        if (StringUtils.startsWith(text, SwissPaymentsCode.QR_TYPE)) {
            try {
                return null != new SwissPaymentsCodeParser().parse(text);
            } catch (Exception e){
                return false;
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                final String preview = text.substring(0, Math.min(10, text.length()));
                LOGGER.debug("QR Code does not start with expected SPC Prefix, but with '{}...'", preview);
            }
            return false;
        }
    }
}
