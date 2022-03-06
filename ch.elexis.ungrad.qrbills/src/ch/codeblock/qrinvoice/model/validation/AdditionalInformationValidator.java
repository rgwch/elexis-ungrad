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

import ch.codeblock.qrinvoice.MappingException;
import ch.codeblock.qrinvoice.model.AdditionalInformation;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.util.StringUtils;

import static ch.codeblock.qrinvoice.model.validation.ValidationUtils.*;

public class AdditionalInformationValidator {
    public void validate(final ValidationOptions validationOptions, final AdditionalInformation additionalInformation, final ValidationResult result) {
        validateUnstructuredMessage(additionalInformation, result);
        validateTrailer(additionalInformation, result);
        validateBillInformation(validationOptions, additionalInformation, result);
        validateMaxLength(additionalInformation, result);
    }

    private void validateMaxLength(final AdditionalInformation additionalInformation, final ValidationResult result) {
        // validate that both unstructured message and bill information do not exceed 140 characters in total
        final int unstructuredMessageLength = StringUtils.length(additionalInformation.getUnstructuredMessage());
        final int billInformationLength = StringUtils.length(additionalInformation.getBillInformation());
        final int additionalInformationTotalLength = unstructuredMessageLength + billInformationLength;
        if (additionalInformationTotalLength > 140) {
            result.addError("paymentReference", "additionalInformation", additionalInformationTotalLength, "{{validation.error.paymentReference.additionalInformation.commonTotal}}");
        }
    }

    private void validateBillInformation(final ValidationOptions validationOptions, final AdditionalInformation additionalInformation, final ValidationResult result) {
        if (!validationOptions.isSkipBillInformationObject() && StringUtils.isNotEmpty(additionalInformation.getBillInformation()) && additionalInformation.getBillInformationObject() != null) {
            if (!additionalInformation.getBillInformation().equals(additionalInformation.getBillInformationObject().toBillInformationString())) {
                new MappingException("Both BillInformation string and BillInformationObject were set but were inconsistent");
                result.addError("paymentReference", "additionalInformation.billInformation", additionalInformation.getBillInformation(), "{{validation.error.paymentReference.additionalInformation.billInformation.inconsistent}}");

            }
        }

        final BillInformationValidator billInformationValidator = BillInformationValidator.create();
        result.includeErrorsFrom(billInformationValidator.validate(additionalInformation.getBillInformation()));
        result.includeErrorsFrom(billInformationValidator.validate(additionalInformation.getBillInformationObject()));
    }

    private void validateTrailer(final AdditionalInformation additionalInformation, final ValidationResult result) {
        final String trailer = additionalInformation.getTrailer();
        validateLength(trailer, 3, 3, (value) -> result.addError("paymentReference.additionalInformation", "trailer", trailer, "{{validation.error.paymentReference.additionalInformation.trailer}}"));
        validateTrue(trailer, SwissPaymentsCode.END_PAYMENT_DATA_TRAILER.equals(trailer), (value) -> result.addError("paymentReference.additionalInformation", "trailer", trailer, "{{validation.error.paymentReference.additionalInformation.trailer}}"));
    }

    private void validateUnstructuredMessage(final AdditionalInformation additionalInformation, final ValidationResult result) {
        final String unstructuredMessage = additionalInformation.getUnstructuredMessage();
        validateOptionalLength(unstructuredMessage, 0, 140, (value) -> result.addError("paymentReference.additionalInformation", "unstructuredMessage", value, "{{validation.error.paymentReference.additionalInformation.unstructuredMessage}}"));
        ValidationUtils.validateString(unstructuredMessage, (value, msgs) -> result.addError("paymentReference.additionalInformation", "unstructuredMessage", value, msgs));
    }

}
