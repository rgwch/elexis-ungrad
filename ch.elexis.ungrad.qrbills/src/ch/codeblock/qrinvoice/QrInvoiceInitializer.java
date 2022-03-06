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
package ch.codeblock.qrinvoice;

import ch.codeblock.qrinvoice.banner.Banner;
import ch.codeblock.qrinvoice.fonts.FontManager;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.paymentpartreceipt.LayoutDefinitions;
import ch.codeblock.qrinvoice.util.CountryUtils;

import java.util.ResourceBundle;

/**
 * {@link QrInvoiceInitializer} can be used to pre-initialize the QR-Invoice library. Pre-Initialization can be useful for two reason:
 * <ol>
 * <li>fail fast - if something is misconfigured one gets fast feedback instead of delayed errors</li>
 * <li>first call performance - if everything is lazy-initialized, first call execution time can be degraded</li>
 * </ol>
 * This initializer mainly addressed the second aspect.
 */
public class QrInvoiceInitializer {
    private static final QrInvoiceInitializer INSTANCE = new QrInvoiceInitializer();

    public static QrInvoiceInitializer create() {
        return INSTANCE;
    }

    private QrInvoiceInitializer() {
    }

    private boolean initialized = false;

    /**
     * Pre-Initialize time consuming components
     */
    public synchronized void preInitialize() {
        if (initialized) {
            return;
        }

        Banner.printBanner();
        // scanning font directories can be a time consuming thing, depending on the number of fonts installed in the given paths
        FontManager.eagerInitialization();
        // just triggers some resource bundle pre-caching - not as a big deal as font scanning but still worth it
        CountryUtils.isValidIsoCode(SwissPaymentsCode.COUNTRY_CODE_SWITZERLAND);
        LayoutDefinitions.SUPPORTED_LOCALES.forEach(locale -> ResourceBundle.getBundle("qrinvoice", locale));
        initialized = true;
    }
}
