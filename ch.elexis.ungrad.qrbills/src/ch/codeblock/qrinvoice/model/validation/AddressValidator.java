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
package ch.codeblock.qrinvoice.model.validation;

import ch.codeblock.qrinvoice.model.*;

import static ch.codeblock.qrinvoice.model.validation.ValidationUtils.*;
import static ch.codeblock.qrinvoice.util.CountryUtils.isValidIsoCode;

public class AddressValidator {
    public void validate(final Creditor creditor, final ValidationResult result) {
        validateAddress("creditorinformation.creditor", creditor, true, result);
    }

    public void validate(final UltimateCreditor ultimateCreditor, final ValidationResult result) {
        validateAddress("ultimateCreditor", ultimateCreditor, false, result);
    }

    public void validate(final UltimateDebtor ultimateDebtor, final ValidationResult result) {
        validateAddress("ultimateDebtor", ultimateDebtor, false, result);
    }

    private void validateAddress(final String baseDataPath, final Address address, final boolean mandatoryGroup, final ValidationResult result) {
        if (address == null) {
            if (mandatoryGroup) {
                result.addError(baseDataPath, null, null, "{{validation.error.address.group}}");
            }
            return;
        }

        final AddressType addressType = address.getAddressType();
        validateNotNull(addressType, (value) -> result.addError(baseDataPath, "addressType", null, "{{validation.error.address.addressType}}"));

        if(addressType != null) {
            // only validate further if address type is set
            switch (addressType) {
                case STRUCTURED:
                    validateStructuredAddress(baseDataPath, address, result);
                    break;
                case COMBINED:
                    validateCombinedAddress(baseDataPath, address, result);
                    break;
            }
        }
    }

    private void validateStructuredAddress(final String baseDataPath, final StructuredAddress address, final ValidationResult result) {

        // @formatter:off
        // lengths
        validateBaseAddress(baseDataPath, address, result);
        validateOptionalLength(address.getStreetName(),  1, 70, (value) -> result.addError(baseDataPath, "streetName",  value, "{{validation.error.address.streetName}}"));
        validateOptionalLength(address.getHouseNumber(), 1, 16, (value) -> result.addError(baseDataPath, "houseNumber", value, "{{validation.error.address.houseNumber}}"));
        validateLength        (address.getPostalCode(),  1, 16, (value) -> result.addError(baseDataPath, "postalCode",  value, "{{validation.error.address.postalCode}}"));
        validateLength        (address.getCity(),        1, 35, (value) -> result.addError(baseDataPath, "city",        value, "{{validation.error.address.city}}"));
        
        // characters
        validateBaseAddressCharacters(baseDataPath, address, result);
        validateString    (address.getStreetName(),  (value, msgs) -> result.addError(baseDataPath, "streetName",  value, msgs));
        validateString    (address.getHouseNumber(), (value, msgs) -> result.addError(baseDataPath, "houseNumber", value, msgs));
        validateString    (address.getPostalCode(),  (value, msgs) -> result.addError(baseDataPath, "postalCode",  value, msgs));
        validateString    (address.getCity(),        (value, msgs) -> result.addError(baseDataPath, "city",        value, msgs));
        // @formatter:on

        // cast to check that addressLine 1 + 2 are not set
        if (address instanceof CombinedAddress) {
            final CombinedAddress combinedAddress = (CombinedAddress) address;
            validateNull(combinedAddress.getAddressLine1(), (value) -> result.addError(baseDataPath, "addressLine1", value, "{{validation.error.address.structured.addressLines}}"));
            validateNull(combinedAddress.getAddressLine2(), (value) -> result.addError(baseDataPath, "addressLine2", value, "{{validation.error.address.structured.addressLines}}"));
        }
    }

    private void validateCombinedAddress(final String baseDataPath, final CombinedAddress address, final ValidationResult result) {

        // @formatter:off
        // lengths
        validateBaseAddress(baseDataPath, address, result);
        validateOptionalLength(address.getAddressLine1(), 1, 70, (value) -> result.addError(baseDataPath, "addressLine1", value, "{{validation.error.address.addressLine1}}"));
        validateLength(address.getAddressLine2(), 1, 70, (value) -> result.addError(baseDataPath, "addressLine2", value, "{{validation.error.address.addressLine2}}", "{{validation.error.address.combined.addressLine2}}"));
        
        // characters
        validateBaseAddressCharacters(baseDataPath, address, result);
        validateString    (address.getAddressLine1(), (value, msgs) -> result.addError(baseDataPath, "addressLine1",  value, msgs));
        validateString    (address.getAddressLine2(), (value, msgs) -> result.addError(baseDataPath, "addressLine2", value, msgs));
        // @formatter:on 

        // cast to check that streetname, housenumber, postalcode and city are not set
        if (address instanceof StructuredAddress) {
            final StructuredAddress structuredAddress = (StructuredAddress) address;
            // @formatter:off
            validateNull(structuredAddress.getStreetName(),  (value) -> result.addError(baseDataPath, "streetName",  value, "{{validation.error.address.combined.streetName}}"));
            validateNull(structuredAddress.getHouseNumber(), (value) -> result.addError(baseDataPath, "houseNumber", value, "{{validation.error.address.combined.houseNumber}}"));
            validateNull(structuredAddress.getPostalCode(),  (value) -> result.addError(baseDataPath, "postalCode",  value, "{{validation.error.address.combined.postalCode}}"));
            validateNull(structuredAddress.getCity(),        (value) -> result.addError(baseDataPath, "city",        value, "{{validation.error.address.combined.city}}"));
            // @formatter:on
        }
    }

    private void validateBaseAddress(final String baseDataPath, final BaseAddress address, final ValidationResult result) {
        validateLength(address.getName(), 1, 70, (value) -> result.addError(baseDataPath, "name", value, "{{validation.error.address.name}}"));
        validateTrue(address.getCountry(), isValidIsoCode(address.getCountry()), (value) -> result.addError(baseDataPath, "country", value, "{{validation.error.address.country}}"));
    }

    private void validateBaseAddressCharacters(final String baseDataPath, final BaseAddress address, final ValidationResult result) {
        validateString(address.getName(), (value, msgs) -> result.addError(baseDataPath, "name", value, msgs));

    }
}
