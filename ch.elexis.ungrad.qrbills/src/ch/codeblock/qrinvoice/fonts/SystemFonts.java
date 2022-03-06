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
package ch.codeblock.qrinvoice.fonts;

import ch.codeblock.qrinvoice.NotYetImplementedException;
import ch.codeblock.qrinvoice.config.SystemProperties;
import ch.codeblock.qrinvoice.util.OperatingSystemUtils;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

class SystemFonts {

    private static final Logger LOGGER = LoggerFactory.getLogger(FontManager.class);
    
    static Set<String> initFontPaths() {
        final Set<String> fontPaths = new LinkedHashSet<>();
        if (StringUtils.isNotEmpty(System.getProperty(SystemProperties.FONTS_DIRECTORY))) {
            fontPaths.add(System.getProperty(SystemProperties.FONTS_DIRECTORY));
        }

        if (!systemFontsIgnored()) {
            switch (OperatingSystemUtils.detectOperatingSystem()) {
                case WINDOWS:
                    fontPaths.add("C:/Windows/Fonts");
                    // e.g. c:/users/<username>/appdata/local/Microsoft/windows/fonts/
                    fontPaths.add(OperatingSystemUtils.getUserHome() + "/appdata/local/Microsoft/windows/fonts/");
                    break;
                case MAC_OS:
                    fontPaths.add("/Library/Fonts");
                    fontPaths.add("/System/Library/Fonts");
                    fontPaths.add("/Network/Library/Fonts");
                    // e.g. /Users/<username>/Library/Fonts
                    fontPaths.add(OperatingSystemUtils.getUserHome() + "/Library/Fonts");
                    break;
                case XNIX:
                    fontPaths.add("/usr/share/fonts");
                    fontPaths.add("/usr/share/X11/fonts");
                    fontPaths.add("/usr/local/share/fonts");
                    fontPaths.add("/usr/X11R6/lib/X11/fonts");
                    // e.g. /home/<username>/.local/share/fonts
                    fontPaths.add(OperatingSystemUtils.getUserHome() + "/.local/share/fonts");
                    fontPaths.add(OperatingSystemUtils.getUserHome() + "/.fonts");
                    break;
                case UNKNOWN:
                    break;
                default:
                    throw new NotYetImplementedException("Missing Operating System case");
            }
        }
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Font paths={}", String.join(";", fontPaths));
        }
        
        return fontPaths;
    }


    static boolean systemFontsIgnored() {
        final boolean ignoreSystemFonts = "true".equalsIgnoreCase(System.getProperty(SystemProperties.IGNORE_SYSTEM_FONTS));
        if (ignoreSystemFonts) {
            LOGGER.info("System Fonts ignored");
        }
        return ignoreSystemFonts;
    }
    
}
