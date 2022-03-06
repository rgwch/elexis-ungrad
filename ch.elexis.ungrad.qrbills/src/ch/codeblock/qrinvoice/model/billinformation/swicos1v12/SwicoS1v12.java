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
package ch.codeblock.qrinvoice.model.billinformation.swicos1v12;

import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.annotation.Description;
import ch.codeblock.qrinvoice.model.annotation.Example;
import ch.codeblock.qrinvoice.model.billinformation.BillInformation;
import ch.codeblock.qrinvoice.model.validation.ValidationResult;
import ch.codeblock.qrinvoice.util.CollectionUtils;
import ch.codeblock.qrinvoice.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Swico S1, Version 1.2 - 23.11.2018<br>
 * <a href="https://www.swiss-qr-invoice.org/downloads/qr-bill-s1-syntax-de.pdf">Syntaxdefinition der Rechnungsinformationen (S1) bei der QR-Rechnung</a>
 */
public class SwicoS1v12 implements BillInformation {
    private String billInformationType = SwicoS1v12.class.getSimpleName();
    private String invoiceReference;
    private LocalDate invoiceDate;
    private String customerReference;
    private String uidNumber;
    private LocalDate vatDateStart;
    private LocalDate vatDateEnd;
    private List<VatDetails> vatDetails;
    private List<ImportTaxPosition> importTaxes;
    private List<PaymentCondition> paymentConditions;

    /**
     * @see Tag#INVOICE_REFERENCE
     */
    @Example("10201409")
    @Description("See Tag 10 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(final String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }

    /**
     * @see Tag#INVOICE_DATE
     */
    @Example("2019-05-12")
    @Description("See Tag 11 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    /**
     * @see Tag#CUSTOMER_REFERENCE
     */
    @Example("1400.000-53")
    @Description("See Tag 20 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(final String customerReference) {
        this.customerReference = customerReference;
    }

    /**
     * @see Tag#VAT_UID_NUMBER
     */
    @Example("106017086")
    @Description("See Tag 30 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public String getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(final String uidNumber) {
        this.uidNumber = uidNumber;
    }

    /**
     * @see Tag#VAT_DATE
     */
    @Example("2018-05-08")
    @Description("See Tag 31 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public LocalDate getVatDateStart() {
        return vatDateStart;
    }

    public void setVatDateStart(final LocalDate vatDateStart) {
        this.vatDateStart = vatDateStart;
    }

    /**
     * @see Tag#VAT_DATE
     */
    @Example("2018-05-10")
    @Description("See Tag 31 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public LocalDate getVatDateEnd() {
        return vatDateEnd;
    }

    public void setVatDateEnd(final LocalDate vatDateEnd) {
        this.vatDateEnd = vatDateEnd;
    }

    /**
     * @see Tag#VAT_DETAILS
     */
    public List<VatDetails> getVatDetails() {
        return vatDetails;
    }

    public void setVatDetails(final List<VatDetails> vatDetails) {
        this.vatDetails = vatDetails;
    }

    public void addVatDetails(final VatDetails vatDetails) {
        if (this.vatDetails == null) {
            this.vatDetails = new ArrayList<>();
        }
        this.vatDetails.add(vatDetails);
    }

    /**
     * @see Tag#IMPORT_TAX
     */
    public List<ImportTaxPosition> getImportTaxes() {
        return importTaxes;
    }

    public void setImportTaxes(final List<ImportTaxPosition> importTaxes) {
        this.importTaxes = importTaxes;
    }

    /**
     * @see Tag#PAYMENT_CONDITIONS
     */
    public List<PaymentCondition> getPaymentConditions() {
        return paymentConditions;
    }

    public void setPaymentConditions(final List<PaymentCondition> paymentConditions) {
        this.paymentConditions = paymentConditions;
    }

    public LocalDate getDefaultPaymentPeriod() {
        return invoiceDate.plus(getDefaultPaymentPeriodDays(), ChronoUnit.DAYS);
    }


    public Integer getDefaultPaymentPeriodDays() {
        final BigDecimal zero = BigDecimal.ZERO;
        return getPaymentConditions().stream()
                .filter(condition -> zero.compareTo(condition.getCashDiscountPercentage()) == 0)
                .map(PaymentCondition::getEligiblePaymentPeriodDays)
                .max(Comparator.comparing(Integer::valueOf)).orElse(null);
    }

    public String toBillInformationString() {
        return SwicoS1v12Type.toStrdBkgInfString(this);
    }

    @Override
    @Example("SwicoS1v12")
    @Description("A unique identifier of the current BillInformation Subtype")
    public String getBillInformationType() {
        return billInformationType;
    }

    public void setBillInformationType(final String billInformationType) {
        // setter only present for REST model generator
        throw new UnsupportedOperationException("overriding billInformationType is not supported");
    }

    @Override
    public void validate(final ValidationResult result) {
        validate(result, null);
    }

    @Override
    public void validate(final ValidationResult result, final QrInvoice qrInvoice) {
        validateUid(result);
        validateVatDate(result);
        validateVatDetails(result);
        validateCrossDependentElements(result, qrInvoice);
    }

    @Override
    public ValidationResult validate() {
        final ValidationResult validationResult = new ValidationResult();
        validate(validationResult, null);
        return validationResult;
    }

    @Override
    public ValidationResult validate(QrInvoice qrInvoice) {
        final ValidationResult validationResult = new ValidationResult();
        validate(validationResult, qrInvoice);
        return validationResult;
    }

    private void validateVatDate(final ValidationResult result) {
        if (vatDateStart == null && vatDateEnd != null) {
            throw new TechnicalException("Implementation error, vatDateStart is null but vatDateEnd is not");
        }

        if (vatDateEnd != null && vatDateStart.isAfter(vatDateEnd)) {
            result.addError("paymentReference.additionalInformation", "billInformation", String.format("%s > %s", vatDateStart, vatDateEnd), "{{validation.error.paymentReference.additionalInformation.billInformation.swicos1v12.vatdate.startafterend}}");

        }
    }

    private void validateVatDetails(final ValidationResult result) {
        if (CollectionUtils.isEmpty(vatDetails)) {
            return;
        }

        if (vatDetails.size() == 1) {
            final VatDetails vatDetailPosition = this.vatDetails.get(0);
            if (vatDetailPosition.getTaxPercentage() == null) {
                result.addError("paymentReference.additionalInformation", "billInformation", "null", "{{validation.error.paymentReference.additionalInformation.billInformation.swicos1v12.vatdetails.singleCondition}}");
            }
        } else {
            for (VatDetails vatDetail : vatDetails) {
                if (vatDetail.getTaxedNetAmount() == null || vatDetail.getTaxPercentage() == null) {
                    result.addError("paymentReference.additionalInformation", "billInformation", "null", "{{validation.error.paymentReference.additionalInformation.billInformation.swicos1v12.vatdetails.listCondition}}");
                }
            }

        }
    }

    private BigDecimal calculateGrossAmount() {
        if (CollectionUtils.isEmpty(vatDetails)) {
            return null;
        }
        if (vatDetails.size() == 1) {
            return null;
        } else {
            BigDecimal totalGrossAmount = BigDecimal.ZERO;
            for (VatDetails vatDetailPosition : vatDetails) {
                totalGrossAmount = totalGrossAmount.add(vatDetailPosition.calculateGrossAmount());
            }
            return totalGrossAmount;
        }
    }

    private void validateUid(final ValidationResult result) {
        if (StringUtils.isNotEmpty(uidNumber) && !SwicoS1v12Type.UID_PATTERN.matcher(uidNumber).matches()) {
            result.addError("paymentReference.additionalInformation", "billInformation", uidNumber, "{{validation.error.paymentReference.additionalInformation.billInformation.swicos1v12.uidnumber}}");
        }
    }

    private void validateCrossDependentElements(final ValidationResult result, final QrInvoice qrInvoice) {
        if (qrInvoice != null) {
            boolean addValidationError = false;
            final BigDecimal grossAmount = calculateGrossAmount();
            if (grossAmount != null) {
                final BigDecimal amountQrInvoice = qrInvoice.getPaymentAmountInformation().getAmount();
                if (amountQrInvoice != null) {
                    final BigDecimal roundingDifferences = amountQrInvoice.subtract(grossAmount).abs();
                    if (roundingDifferences.doubleValue() > 0.05) {
                        addValidationError = true;
                    }
                } else {
                    addValidationError = true;
                }
            }

            if (addValidationError) {
                result.addError("paymentReference.additionalInformation", "billInformation", uidNumber, "{{validation.error.paymentReference.additionalInformation.billInformation.swicos1v12.vatdetails.amountmismatch}}");
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SwicoS1v12 that = (SwicoS1v12) o;
        return Objects.equals(invoiceReference, that.invoiceReference) &&
                Objects.equals(invoiceDate, that.invoiceDate) &&
                Objects.equals(customerReference, that.customerReference) &&
                Objects.equals(uidNumber, that.uidNumber) &&
                Objects.equals(vatDateStart, that.vatDateStart) &&
                Objects.equals(vatDateEnd, that.vatDateEnd) &&
                Objects.equals(vatDetails, that.vatDetails) &&
                Objects.equals(importTaxes, that.importTaxes) &&
                Objects.equals(paymentConditions, that.paymentConditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceReference, invoiceDate, customerReference, uidNumber, vatDateStart, vatDateEnd, vatDetails, importTaxes, paymentConditions);
    }
}
