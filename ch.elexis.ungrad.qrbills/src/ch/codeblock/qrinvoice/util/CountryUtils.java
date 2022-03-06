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

import java.util.Locale;
import java.util.ResourceBundle;

import static ch.codeblock.qrinvoice.model.SwissPaymentsCode.COUNTRY_CODE_LIECHTENSTEIN;
import static ch.codeblock.qrinvoice.model.SwissPaymentsCode.COUNTRY_CODE_SWITZERLAND;

/**
 * ISO 3166-1 Country Util
 */
public final class CountryUtils {
    private CountryUtils() {
    }

    private static final ResourceBundle COUNTRIES = getCountries();

    /**
     * <p>Checks whether the given country code is a valid ISO 3166-1 alpha-2 country code</p>
     *
     * <p><b>Important:</b> country code must be passed in upper case, lower or mixed case will always return false</p>
     *
     * @param countryCode The country code string to validate (2 characters expected)
     * @return True, if the countryCode is a valid ISO code
     */
    public static boolean isValidIsoCode(final String countryCode) {
        if (COUNTRY_CODE_SWITZERLAND.equals(countryCode) || COUNTRY_CODE_LIECHTENSTEIN.equals(countryCode)) {
            // CH / LI will be the most used country codes, thus quick validation step
            return true;
        } else if (countryCode == null || countryCode.length() != 2) {
            return false;
        }
        return COUNTRIES.containsKey(countryCode);
    }

    public static ResourceBundle getCountries() {
        return getCountries(Locale.ENGLISH);
    }
    
    public static ResourceBundle getCountries(final Locale Locale) {
        return ResourceBundle.getBundle("ch.codeblock.qrinvoice.standards.iso3166-1", Locale);
    }
}
