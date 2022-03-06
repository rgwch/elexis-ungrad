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

import ch.codeblock.qrinvoice.model.annotation.Description;

public enum AddressType {
    /**
     * <p>Structured Address</p>
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>Description</th></tr>
     * <tr><td>EN</td><td>"S" - structured address</td></tr>
     * <tr><td>DE</td><td>"S" - Strukturierte Adresse</td></tr>
     * <tr><td>FR</td><td>"S" - Adresse structurée</td></tr>
     * <tr><td>IT</td><td></td></tr>
     * </table>
     *
     * @see Address#getAddressType() 
     * @see Creditor#getAddressType() 
     * @see UltimateCreditor#getAddressType() 
     * @see UltimateDebtor#getAddressType() 
     */
    @Description("\"S\" - structured address")
    STRUCTURED('S'),
    /**
     * <p>Combined Address</p>
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>Description</th></tr>
     * <tr><td>EN</td><td>"K" - combined address elements (2 lines)</td></tr>
     * <tr><td>DE</td><td>"K" - Kombinierte Adressfelder (2 Zeilen)</td></tr>
     * <tr><td>FR</td><td>"K" - Champs d'adresse combinés (2 lignes)</td></tr>
     * <tr><td>IT</td><td></td></tr>
     * </table>
     *
     * @see Address#getAddressType()
     * @see Creditor#getAddressType()
     * @see UltimateCreditor#getAddressType()
     * @see UltimateDebtor#getAddressType()
     */
    @Description("\"K\" - combined address elements (2 lines)")
    COMBINED('K');
    
    private final char addressTypeCode;

    AddressType(final char addressTypeCode) {
        this.addressTypeCode = addressTypeCode;
    }

    public char getAddressTypeCode() {
        return addressTypeCode;
    }
    
    public String getAddressTypeCodeAsString() {
        return String.valueOf(addressTypeCode);
    }


    public static AddressType parse(final char addressTypeCode) {
        for (final AddressType addressType : values()) {
            if (addressType.getAddressTypeCode() == addressTypeCode) {
                return addressType;
            }
        }
        throw new ParseException("Invalid address type '" + addressTypeCode + "' given");
    }
    
    public static AddressType parse(final String addressTypeCode) {
        if(addressTypeCode == null) {
            return null;
        }
        if(addressTypeCode.length() != 1) {
            throw new ParseException("Invalid address type '" + addressTypeCode + "' given. If set must be a string with a length of 1");
        }
        return parse(addressTypeCode.charAt(0));
    }

    
}
