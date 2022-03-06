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

import ch.codeblock.qrinvoice.model.Address;
import ch.codeblock.qrinvoice.model.AddressType;

public abstract class AddressBuilderBase<T extends AddressBuilderBase, U extends Address> implements StructuredAddressBuilder, CombinedAddressBuilder, AddressBuilder {
    protected AddressType addressType;
    protected String name;

    // structured
    protected String streetName;
    protected String houseNumber;
    protected String postalCode;
    protected String city;

    // combined
    protected String addressLine1;
    protected String addressLine2;

    protected String country;

    @Override
    public StructuredAddressBuilder structuredAddress() {
        return addressType(AddressType.STRUCTURED);
    }

    @Override
    public CombinedAddressBuilder combinedAddress() {
        return addressType(AddressType.COMBINED);
    }

    /**
     * @param addressType The address type to set
     * @return The {@link T} instance
     * @see Address#getAddressType() ()
     */
    public T addressType(AddressType addressType) {
        this.addressType = addressType;
        return (T) this;
    }

    /**
     * @param name The name to set
     * @return The {@link T} instance
     * @see Address#getName()
     */
    @Override
    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    /**
     * @param streetName The streetName to set
     * @return The {@link T} instance
     * @see Address#getStreetName()
     */
    @Override
    public T streetName(String streetName) {
        this.streetName = streetName;
        return (T) this;
    }

    /**
     * @param houseNumber The houseNumber to set
     * @return The {@link T} instance
     * @see Address#getHouseNumber()
     */
    @Override
    public T houseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        return (T) this;
    }

    /**
     * @param postalCode The postalCode to set
     * @return The {@link T} instance
     * @see Address#getPostalCode()
     */
    @Override
    public T postalCode(String postalCode) {
        this.postalCode = postalCode;
        return (T) this;
    }

    /**
     * @param city The city to set
     * @return The {@link T} instance
     * @see Address#getCity()
     */
    @Override
    public T city(String city) {
        this.city = city;
        return (T) this;
    }

    /**
     * @param addressLine1 The address line 1 to set
     * @return The {@link T} instance
     * @see Address#getAddressLine1()
     */
    @Override
    public T addressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return (T) this;
    }

    /**
     * @param addressLine2 The address line 2 to set
     * @return The {@link T} instance
     * @see Address#getAddressLine2()
     */
    @Override
    public T addressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return (T) this;
    }

    /**
     * @param country The country to set
     * @return The {@link T} instance
     * @see Address#getCountry()
     */
    @Override
    public T country(String country) {
        this.country = country;
        return (T) this;
    }

    protected <U extends Address> U setData(U address) {
        address.setAddressType(addressType);
        address.setName(name);

        // structured
        address.setStreetName(streetName);
        address.setHouseNumber(houseNumber);
        address.setPostalCode(postalCode);
        address.setCity(city);

        // combined
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);

        address.setCountry(country);

        return address;
    }

    public abstract U build();
}
