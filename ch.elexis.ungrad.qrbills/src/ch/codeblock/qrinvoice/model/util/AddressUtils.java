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
package ch.codeblock.qrinvoice.model.util;

import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.model.Address;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AddressUtils {

    private static final String COUNTRY_CODE_SEPARATOR = " - ";

    private AddressUtils() {
    }

    public static boolean isEmpty(Address address) {
        return (address == null) || (
                address.getAddressType() == null &&
                        StringUtils.isEmpty(address.getName()) &&
                        StringUtils.isEmpty(address.getCountry()) &&

                        // structured
                        StringUtils.isEmpty(address.getStreetName()) &&
                        StringUtils.isEmpty(address.getHouseNumber()) &&
                        StringUtils.isEmpty(address.getPostalCode()) &&
                        StringUtils.isEmpty(address.getCity()) &&

                        // combined
                        StringUtils.isEmpty(address.getAddressLine1()) &&
                        StringUtils.isEmpty(address.getAddressLine2()));
    }

    public static List<String> toAddressLines(final Address address) {
        return toAddressLines(address, false);
    }

    /**
     * According to the spec 3.6.2 - Information section - v2.0<br>
     * <p>"Because of the limited space, it is permitted to omit the street name and building number from the addresses of the creditor (Payable to) and the debtor (Payable by)".</p>
     *
     * @param address                                 The address to build the addresse lines
     * @param omitStreetNameHouseNumberOrAddressLine1 if true, streetname / housenumber or addressline1 is not returned as address line (used in some cases for the receipt)
     * @return a list of address lines
     */
    public static List<String> toAddressLines(final Address address, final boolean omitStreetNameHouseNumberOrAddressLine1) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        if (address.getAddressType() == null) {
            throw new TechnicalException("Missing address type is unexpected at this point");
        }
        switch (address.getAddressType()) {
            case STRUCTURED:
                return toAddressLines(
                        address.getName(),
                        address.getStreetName(),
                        address.getHouseNumber(),
                        address.getPostalCode(),
                        address.getCity(),
                        address.getCountry(),
                        omitStreetNameHouseNumberOrAddressLine1);
            case COMBINED:
                return toAddressLines(
                        address.getName(),
                        address.getAddressLine1(),
                        address.getAddressLine2(),
                        address.getCountry(),
                        omitStreetNameHouseNumberOrAddressLine1);
            default:
                throw new TechnicalException("Address type is unknown");
        }
    }

    private static List<String> toAddressLines(final String name, final String addressLine1, final String addressLine2, final String country, final boolean omitAddressLine1) {
        final boolean hasName = StringUtils.isNotEmpty(name);
        final boolean hasAddressLine1 = StringUtils.isNotEmpty(addressLine1);
        final boolean hasAddressLine2 = StringUtils.isNotEmpty(addressLine2);
        final boolean printCountryCode = printCountryCode(country);

        final boolean line1 = hasName;
        final boolean line2 = hasAddressLine1 && !omitAddressLine1;
        final boolean line3 = hasAddressLine2 || printCountryCode;

        final List<String> addressLines = new ArrayList<>(3);
        if (line1) {
            addressLines.add(name);
        }

        if (line2) {
            addressLines.add(addressLine1);
        }

        if (line3) {
            if (printCountryCode && hasAddressLine2) {
                addressLines.add(country + COUNTRY_CODE_SEPARATOR + addressLine2);
            } else if (hasAddressLine2) {
                addressLines.add(addressLine2);
            }
        }

        return addressLines;
    }

    private static List<String> toAddressLines(final String name, final String streetName, final String houseNumber, final String postalCode, final String city,
                                               final String country, final boolean omitStreetNameHouseNumber) {
        final boolean hasName = StringUtils.isNotEmpty(name);
        final boolean hasStreetName = StringUtils.isNotEmpty(streetName);
        final boolean hasHouseNumber = StringUtils.isNotEmpty(houseNumber);
        final boolean hasPostalCode = StringUtils.isNotEmpty(postalCode);
        final boolean hasCity = StringUtils.isNotEmpty(city);

        final boolean line1 = hasName;
        final boolean line2 = (hasStreetName || hasHouseNumber) && !omitStreetNameHouseNumber;
        final boolean line3 = hasPostalCode || hasCity;

        final List<String> addressLines = new ArrayList<>(3);
        if (line1) {
            addressLines.add(name);
        }

        if (line2) {
            addLine2(streetName, houseNumber, hasStreetName, hasHouseNumber, addressLines);
        }

        if (line3) {
            addLine3(postalCode, city, hasPostalCode, hasCity, country, addressLines);
        }

        return addressLines;
    }

    private static void addLine2(final String streetName, final String houseNumber, final boolean hasStreetName, final boolean hasHouseNumber, final List<String> addressLines) {
        final StringBuilder sb = new StringBuilder();
        if (hasStreetName) {
            sb.append(streetName);
        }
        if (hasStreetName && hasHouseNumber) {
            sb.append(" ");
        }
        if (hasHouseNumber) {
            sb.append(houseNumber);
        }
        addressLines.add(sb.toString());
    }

    private static void addLine3(final String postalCode, final String city, final boolean hasPostalCode, final boolean hasCity, final String country, final List<String> addressLines) {
        final boolean printCountryCode = printCountryCode(country);
        
        final StringBuilder sb = new StringBuilder();
        if(printCountryCode) {
            sb.append(country);
            if(hasPostalCode || hasCity) {
                sb.append(COUNTRY_CODE_SEPARATOR);
            }
        }
        
        if (hasPostalCode) {
            sb.append(postalCode);
        }
        if (hasPostalCode && hasCity) {
            sb.append(" ");
        }
        if (hasCity) {
            sb.append(city);
        }
        addressLines.add(sb.toString());
    }

    private static boolean printCountryCode(final String country) {
        return country != null && !SwissPaymentsCode.COUNTRY_CODES_CH_LI.contains(country);
    }

    public static String toSingleLineAddress(final Address address) {
        final List<String> addressLines = toAddressLines(address);
        if (addressLines == null) {
            return "";
        } else {
            return String.join(", ", addressLines);
        }
    }
}
