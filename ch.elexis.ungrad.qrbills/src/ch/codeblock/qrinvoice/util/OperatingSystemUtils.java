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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperatingSystemUtils {
    private OperatingSystemUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemUtils.class);

    public enum OperatingSystem {
        WINDOWS,
        MAC_OS,
        /**
         * Linux, Unix, BSD
         */
        XNIX,
        UNKNOWN,

    }
    
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static OperatingSystem detectOperatingSystem() {
        final String osName = System.getProperty("os.name");
        LOGGER.debug("os.name = '{}'", osName);

        return detectOperatingSystem(osName);
    }

    static OperatingSystem detectOperatingSystem(final String osName) {
        final String osLowerCase = osName.toLowerCase();
        if (osLowerCase.contains("window")) {
            return OperatingSystem.WINDOWS;
        } else if (osLowerCase.contains("mac os") ||
                osLowerCase.contains("darwin")) {
            return OperatingSystem.MAC_OS;
        } else if (osLowerCase.contains("linux") ||
                osLowerCase.contains("unix") ||
                osLowerCase.contains("freebsd") ||
                osLowerCase.contains("openbsd")) {
            return OperatingSystem.XNIX;
        } else {
            LOGGER.warn("Operating System could not be determined. os.name = '{}'", osName);
            return OperatingSystem.UNKNOWN;
        }
    }
}
