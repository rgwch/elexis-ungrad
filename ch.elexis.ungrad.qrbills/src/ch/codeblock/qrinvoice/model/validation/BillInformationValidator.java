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

import ch.codeblock.qrinvoice.config.SystemProperties;
import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.model.billinformation.BillInformation;
import ch.codeblock.qrinvoice.model.parser.BillInformationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Pattern;

import static ch.codeblock.qrinvoice.model.validation.ValidationUtils.validateOptionalLength;
import static ch.codeblock.qrinvoice.model.validation.ValidationUtils.validateString;

public class BillInformationValidator {
    private final Logger logger = LoggerFactory.getLogger(BillInformationValidator.class);

    private static final Pattern BILL_INFORMATION_PATTERN = Pattern.compile(SwissPaymentsCode.BILL_INFORMATION_REGEX_PATTERN);

    private BillInformationValidator() {
    }

    public static BillInformationValidator create() {
        return new BillInformationValidator();
    }

    public ValidationResult validate(final QrInvoice qrInvoice, final String billInformation) {
        final ValidationResult result = new ValidationResult();

        validateRawString(billInformation, result);

        if (billInformationValidationEnabled()) {
            if (billInformation != null && !BILL_INFORMATION_PATTERN.matcher(billInformation).matches()) {
                result.addError("paymentReference.additionalInformation", "billInformation", billInformation, "{{validation.error.paymentReference.additionalInformation.billInformation.startPattern}}");
            }

            try {
                final Optional<BillInformation> billInformationObjectOptional = parseBillInformation(billInformation);
                billInformationObjectOptional.ifPresent(billInformationObject -> billInformationObject.validate(result, qrInvoice));
            } catch (Exception e) {
                logger.debug("Unable to parse BillInformation. Reason={}", e.getMessage());
            }
        } else {
            logDisableBillInformationFlag();
        }
        return result;
    }

    public void validateRawString(final String billInformation, final ValidationResult result) {
        validateOptionalLength(billInformation, 0, 140, (value) -> result.addError("paymentReference.additionalInformation", "billInformation", value, "{{validation.error.paymentReference.additionalInformation.billInformation}}"));
        validateString(billInformation, (value, msgs) -> result.addError("paymentReference.additionalInformation", "billInformation", value, msgs));
    }

    public ValidationResult validate(final String billInformation) {
        return validate(null, billInformation);
    }

    public ValidationResult validate(final QrInvoice qrInvoice, final BillInformation billInformation) {
        return billInformation.validate(qrInvoice);
    }

    public ValidationResult validate(final BillInformation billInformation) {
        if (billInformation != null && billInformationValidationEnabled()) {
            return billInformation.validate();
        } else {
            logDisableBillInformationFlag();
            return new ValidationResult();
        }
    }

    private boolean billInformationValidationEnabled() {
        return System.getProperty(SystemProperties.DISABLE_BILL_INFORMATION_VALIDATION) == null;
    }

    private Optional<BillInformation> parseBillInformation(final String billInformation) {
        return Optional.ofNullable(BillInformationParser.create().parseBillInformation(billInformation));
    }

    private void logDisableBillInformationFlag() {
        if (logger.isDebugEnabled()) {
            logger.debug("DISABLE_BILL_INFORMATION_VALIDATION={}", System.getProperty(SystemProperties.DISABLE_BILL_INFORMATION_VALIDATION));
        }
    }


}
