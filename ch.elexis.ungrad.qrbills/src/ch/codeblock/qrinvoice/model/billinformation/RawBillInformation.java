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
package ch.codeblock.qrinvoice.model.billinformation;

import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.annotation.Description;
import ch.codeblock.qrinvoice.model.annotation.Example;
import ch.codeblock.qrinvoice.model.validation.BillInformationValidator;
import ch.codeblock.qrinvoice.model.validation.ValidationResult;

public class RawBillInformation implements BillInformation {
    private String billInformationType = RawBillInformation.class.getSimpleName();
    private String rawBillInformation;

    public RawBillInformation() {
    }

    public RawBillInformation(String rawBillInformation) {
        this.rawBillInformation = rawBillInformation;
    }

    @Override
    @Example("RawBillInformation")
    @Description("A unique identifier of the current BillInformation Subtype")
    public String getBillInformationType() {
        return billInformationType;
    }

    public void setBillInformationType(final String billInformationType) {
        // setter only present for REST model generator
        throw new UnsupportedOperationException("overriding billInformationType is not supported");
    }

    @Example("//XY/...")
    @Description("A raw bill information string. Prefix pattern is defined by the implementation guidelines QR bill")
    public String getRawBillInformation() {
        return rawBillInformation;
    }

    public void setRawBillInformation(final String rawBillInformation) {
        this.rawBillInformation = rawBillInformation;
    }

    @Override
    public ValidationResult validate() {
        final ValidationResult validationResult = new ValidationResult();
        validate(validationResult, null);
        return validationResult;
    }

    @Override
    public ValidationResult validate(final QrInvoice qrInvoice) {
        final ValidationResult validationResult = new ValidationResult();
        validate(validationResult, qrInvoice);
        return validationResult;
    }

    @Override
    public void validate(final ValidationResult result, final QrInvoice qrInvoice) {
        BillInformationValidator.create().validateRawString(rawBillInformation, result);
    }

    @Override
    public void validate(final ValidationResult result) {
        validate(result, null);
    }

    @Override
    public String toBillInformationString() {
        return rawBillInformation;
    }
}
