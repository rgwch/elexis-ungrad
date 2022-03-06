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
package ch.codeblock.qrinvoice.model.builder;

public interface CombinedAddressBuilder {

    /**
     * @param name The name to set
     * @return The {@link CombinedAddressBuilder} instance
     * @see ch.codeblock.qrinvoice.model.Address#getName()
     */
    CombinedAddressBuilder name(String name);

    /**
     * @param addressLine1 The address line 1 to set
     * @return The {@link CombinedAddressBuilder} instance
     * @see ch.codeblock.qrinvoice.model.Address#getAddressLine1()
     */
    CombinedAddressBuilder addressLine1(String addressLine1);

    /**
     * @param addressLine2 The address line 2 to set
     * @return The {@link CombinedAddressBuilder} instance
     * @see ch.codeblock.qrinvoice.model.Address#getAddressLine2()
     */
    CombinedAddressBuilder addressLine2(String addressLine2);

    /**
     * @param country The country to set
     * @return The {@link CombinedAddressBuilder} instance
     * @see ch.codeblock.qrinvoice.model.Address#getCountry()
     */
    CombinedAddressBuilder country(String country);
}
