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

import ch.codeblock.qrinvoice.NotYetImplementedException;
import ch.codeblock.qrinvoice.config.SystemProperties;
import ch.codeblock.qrinvoice.model.*;
import ch.codeblock.qrinvoice.model.util.AddressUtils;
import ch.codeblock.qrinvoice.util.CollectionUtils;
import ch.codeblock.qrinvoice.util.CreditorReferenceUtils;
import ch.codeblock.qrinvoice.util.IbanUtils;
import ch.codeblock.qrinvoice.util.QRReferenceUtils;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static ch.codeblock.qrinvoice.model.SwissPaymentsCode.*;
import static ch.codeblock.qrinvoice.model.validation.ValidationUtils.*;

public class QrInvoiceValidator {
    public static QrInvoiceValidator create() {
        return new QrInvoiceValidator();
    }

    private final AddressValidator addressValidator = new AddressValidator();
    private final AdditionalInformationValidator additionalInformationValidator = new AdditionalInformationValidator();

    public ValidationResult validate(QrInvoice qrInvoice, ValidationOptions validationOptions) {

        final ValidationResult validationResult = new ValidationResult();

        validate(qrInvoice.getHeader(), validationResult);
        validate(qrInvoice.getCreditorInformation(), validationResult);
        validate(qrInvoice.getUltimateCreditor(), validationResult);
        validate(qrInvoice.getUltimateDebtor(), validationResult);
        validate(qrInvoice.getPaymentAmountInformation(), validationResult);
        validate(qrInvoice.getPaymentReference(), validationOptions, validationResult);
        validate(qrInvoice.getAlternativeSchemes(), validationResult);

        validateIbanReference(qrInvoice, validationResult);
        validateDoNotUseForPayment(qrInvoice, validationOptions, validationResult);

        return validationResult;
    }

    public ValidationResult validate(QrInvoice qrInvoice) {
        return validate(qrInvoice, new ValidationOptions());
    }

    private void validate(final Header header, final ValidationResult result) {
        final String qrType = header.getQrType();
        validateLength(qrType, 3, 3, (value) -> result.addError("header", "qrType", qrType, "{{validation.error.qrType}}"));
        validateTrue(qrType, SwissPaymentsCode.QR_TYPE.equals(qrType), (value) -> result.addError("header", "qrType", qrType, "{{validation.error.qrType}}"));

        final byte codingType = header.getCodingType();
        validateRange((int) codingType, 1, 9, (value) -> result.addError("header", "codingType", codingType, "{{validation.error.codingType}}"));
        validateTrue(codingType, codingType == SwissPaymentsCode.CODING_TYPE, (value) -> result.addError("header", "codingType", codingType, "{{validation.error.codingType}}"));

        final short version = header.getVersion();
        // length is 4, but can have leading zero ("0200" equals 2.00)
        validateRange((int) version, 100, 9999, (value) -> result.addError("header", "version", version, "{{validation.error.version}}"));
        validateTrue(version, SwissPaymentsCode.isVersionSupported(version), (value) -> result.addError("header", "version", version, "{{validation.error.version}}"));
    }

    private void validate(final CreditorInformation creditorInformation, final ValidationResult result) {
        addressValidator.validate(creditorInformation.getCreditor(), result);

        final String iban = creditorInformation.getIban();
        // in the spec it says fixed length 21 - but the six validation platform has a length of 18 up to 21 defined. so we choose the relaxed one. IBAN is validated anyway
        validateLength(iban, 18, 21, (value) -> result.addError("creditorInformation", "iban", value, "{{validation.error.iban}}"));
        validateTrue(iban, IbanUtils.isValidIBAN(iban, true), (value) -> result.addError("creditorInformation", "iban", value, "{{validation.error.iban}}"));
    }

    private void validate(final UltimateCreditor ultimateCreditor, final ValidationResult result) {
        if (!AddressUtils.isEmpty(ultimateCreditor) && System.getProperty(SystemProperties.UNLOCK_ULTIMATE_CREDITOR) == null) {
            result.addError("ultimateCreditor", null, null, "{{validation.error.ultimateCreditor.mustNotBeUsed}}");
        }
        addressValidator.validate(ultimateCreditor, result);
    }

    private void validate(final UltimateDebtor ultimateDebtor, final ValidationResult result) {
        addressValidator.validate(ultimateDebtor, result);
    }

    private void validate(final PaymentAmountInformation paymentAmountInformation, final ValidationResult result) {
        final BigDecimal amount = paymentAmountInformation.getAmount();
        if (amount != null) {
            validateRange(amount, AMOUNT_MIN, AMOUNT_MAX, (value) -> result.addError("paymentAmountInformation", "amount", amount, "{{validation.error.amount}}"));
        }

        final Currency currency = paymentAmountInformation.getCurrency();
        validateTrue(currency, SUPPORTED_CURRENCIES.contains(currency), (value) -> result.addError("paymentAmountInformation", "currency", value, "{{validation.error.currency}}"));
    }

    private void validate(final PaymentReference paymentReference, final ValidationOptions validationOptions, final ValidationResult result) {
        final ReferenceType referenceType = paymentReference.getReferenceType();
        validateTrue(getReferenceTypeCode(referenceType), referenceType != null, (value) -> result.addError("paymentReference", "referenceType", value, "{{validation.error.referenceType}}"));

        final String reference = paymentReference.getReference();
        ValidationUtils.validateString(reference, (value, msgs) -> result.addError("paymentReference", "reference", value, msgs));
        if (referenceType != null) {
            switch (referenceType) {
                case QR_REFERENCE:
                    // QR reference: 27 characters, numeric, check sum calculation according to Modulo 10 recursive (27th position of the reference)
                    validateTrue(reference, QRReferenceUtils.isValidQrReference(reference), (value) -> result.addError("paymentReference", "reference", value, "{{validation.error.reference}}", "{{validation.error.reference.QRR}}"));
                    break;
                case CREDITOR_REFERENCE:
                    // Creditor Reference (ISO 11649): up to 25 characters, alphanumeric
                    validateTrue(reference, CreditorReferenceUtils.isValidCreditorReference(reference), (value) -> result.addError("paymentReference", "reference", value, "{{validation.error.reference}}", "{{validation.error.reference.SCOR}}"));
                    break;
                case WITHOUT_REFERENCE:
                    // The element may not be filled for the NON reference type.
                    validateEmpty(reference, (value) -> result.addError("paymentReference", "reference", value, "{{validation.error.reference}}", "{{validation.error.reference.NON}}"));
                    break;
                default:
                    throw new NotYetImplementedException("ReferenceType '" + reference + "' is not yet implemented");
            }
        }

        additionalInformationValidator.validate(validationOptions, paymentReference.getAdditionalInformation(), result);
    }

    private void validate(final AlternativeSchemes alternativeSchemes, final ValidationResult result) {
        if (alternativeSchemes != null) {
            final List<String> alternativeSchemeParameters = alternativeSchemes.getAlternativeSchemeParameters();
            final int size = CollectionUtils.size(alternativeSchemeParameters);
            if (size > MAX_ALT_PMT) {
                result.addError("alternativeSchemes", "alternativeSchemeParameters", size, "{{validation.error.alternativeSchemes.alternativeSchemeParameters.size}}");
            }
            if (size > 0) {
                for (final String param : alternativeSchemeParameters) {
                    validateOptionalLength(param, 0, 100, (value) -> result.addError("alternativeSchemes", "alternativeSchemeParameters", value, "{{validation.error.alternativeSchemes.alternativeSchemeParameter.length}}"));
                    ValidationUtils.validateString(param, (value, msgs) -> result.addError("alternativeSchemes", "alternativeSchemeParameters", value, msgs));
                }
            }
        }
    }

    private void validateIbanReference(final QrInvoice qrInvoice, final ValidationResult result) {
        final String iban = qrInvoice.getCreditorInformation().getIban();
        final ReferenceType referenceType = qrInvoice.getPaymentReference().getReferenceType();
        if (IbanUtils.isQrIBAN(iban)) {
            if (referenceType != ReferenceType.QR_REFERENCE) {
                // with the use of a QR-IBAN a QR-Invoice must contain a QR-Reference
                result.addError("paymentReference", "referenceType", getReferenceTypeCode(referenceType), "{{validation.error.referenceType.qrIban}}");
            }
        } else {
            if (referenceType == ReferenceType.QR_REFERENCE) {
                // QRR must not be used with non QR-IBAN
                result.addError("paymentReference", "referenceType", getReferenceTypeCode(referenceType), "{{validation.error.referenceType.iban}}");
            }
        }
    }


    private void validateDoNotUseForPayment(final QrInvoice qrInvoice, final ValidationOptions validationOptions, final ValidationResult result) {
        if (validationOptions.isSkipDoNotUseForPayment() || System.getProperty(SystemProperties.DISABLE_DO_NOT_USE_FOR_PAYMENT_VALIDATION) != null) {
            return;
        }

        final BigDecimal amount = qrInvoice.getPaymentAmountInformation().getAmount();
        final String unstructuredMessage = qrInvoice.getPaymentReference().getAdditionalInformation().getUnstructuredMessage();

        final boolean amountZero = amount != null && BigDecimal.ZERO.compareTo(amount) == 0;
        final boolean unstructuredMessageDoNoUseForPayment = DoNotUseForPayment.UNSTRUCTURED_MESSAGES.contains(unstructuredMessage);

        if (amountZero && !unstructuredMessageDoNoUseForPayment) {
            result.addError("paymentReference.additionalInformation", "unstructuredMessage", unstructuredMessage, "{{validation.error.doNotUseForPayment.nonEmptyUnstructuredMessage}}");
        } else if (!amountZero && unstructuredMessageDoNoUseForPayment) {
            result.addError("paymentAmountInformation", "amount", amount, "{{validation.error.doNotUseForPayment.nonZeroAmount}}");
        }
    }

    private String getReferenceTypeCode(final ReferenceType referenceType) {
        return referenceType != null ? referenceType.getReferenceTypeCode() : null;
    }

    public CharacterValidationResult validateString(final String input) {
        return ValidationUtils.validateCharacters(input);
    }

    public boolean isStringValid(final String input) {
        return ValidationUtils.validateCharacters(input).isValid();
    }

}
