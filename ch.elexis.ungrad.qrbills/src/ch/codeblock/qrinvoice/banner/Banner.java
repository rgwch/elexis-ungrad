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
package ch.codeblock.qrinvoice.banner;

import ch.codeblock.qrinvoice.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Banner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Banner.class);

    private static boolean printed;

    public static void printBanner() {
        if(System.getProperty(SystemProperties.DISABLE_BANNER) != null) {
            return;
        }
        if (!printed) {
            try {
                if(LOGGER.isInfoEnabled()) {
                    LOGGER.info(readBanner());
                }
                printed = true;
            } catch (IOException e) {
                LOGGER.warn("Unexpected exception during Banner printing");
            }
        }

    }

    static String readBanner() throws IOException {
        final ByteArrayOutputStream banner = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        final InputStream inputStream = Banner.class.getResourceAsStream("/ch/codeblock/qrinvoice/banner.txt");
        while ((length = inputStream.read(buffer)) != -1) {
            banner.write(buffer, 0, length);
        }
        return banner.toString(StandardCharsets.UTF_8.name());
    }

    private Banner() {
    }
}
