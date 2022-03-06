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
package ch.codeblock.qrinvoice.model.billinformation.swicos1v12.builder;

import ch.codeblock.qrinvoice.model.billinformation.swicos1v12.ImportTaxPosition;
import ch.codeblock.qrinvoice.model.billinformation.swicos1v12.PaymentCondition;
import ch.codeblock.qrinvoice.model.billinformation.swicos1v12.SwicoS1v12;
import ch.codeblock.qrinvoice.model.billinformation.swicos1v12.VatDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SwicoS1v12Builder {
    private String invoiceReference;
    private LocalDate invoiceDate;
    private String customerReference;
    private String uidNumber;
    private LocalDate vatDateStart;
    private LocalDate vatDateEnd;
    private List<VatDetails> vatDetails;
    private List<ImportTaxPosition> importTaxes;
    private List<PaymentCondition> paymentConditions;

    private SwicoS1v12Builder() {
    }

    public static SwicoS1v12Builder create() {
        return new SwicoS1v12Builder();
    }

    public SwicoS1v12Builder invoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
        return this;
    }

    public SwicoS1v12Builder invoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public SwicoS1v12Builder customerReference(String customerReference) {
        this.customerReference = customerReference;
        return this;
    }

    public SwicoS1v12Builder uidNumber(String uidNumber) {
        this.uidNumber = uidNumber;
        return this;
    }

    public SwicoS1v12Builder vatDateStart(LocalDate vatDateStart) {
        this.vatDateStart = vatDateStart;
        return this;
    }

    public SwicoS1v12Builder vatDateEnd(LocalDate vatDateEnd) {
        this.vatDateEnd = vatDateEnd;
        return this;
    }

    public SwicoS1v12Builder vatDetails(VatDetails... vatDetails) {
        return vatDetails(Arrays.asList(vatDetails));
    }

    public SwicoS1v12Builder vatDetails(List<VatDetails> vatDetails) {
        vatDetails().addAll(vatDetails);
        return this;
    }

    private List<VatDetails> vatDetails() {
        if (this.vatDetails == null) {
            this.vatDetails = new ArrayList<>();
        }
        return this.vatDetails;
    }

    public SwicoS1v12Builder importTaxes(ImportTaxPosition... importTaxPositions) {
        return importTaxes(Arrays.asList(importTaxPositions));
    }

    public SwicoS1v12Builder importTaxes(List<ImportTaxPosition> importTaxes) {
        importTaxes().addAll(importTaxes);
        return this;
    }

    private List<ImportTaxPosition> importTaxes() {
        if (this.importTaxes == null) {
            this.importTaxes = new ArrayList<>();
        }
        return this.importTaxes;
    }

    public SwicoS1v12Builder paymentConditions(PaymentCondition... paymentConditions) {
        return paymentConditions(Arrays.asList(paymentConditions));
    }

    public SwicoS1v12Builder paymentConditions(List<PaymentCondition> paymentConditions) {
        paymentConditions().addAll(paymentConditions);
        return this;
    }


    private List<PaymentCondition> paymentConditions() {
        if (this.paymentConditions == null) {
            this.paymentConditions = new ArrayList<>();
        }
        return this.paymentConditions;
    }

    public SwicoS1v12 build() {
        SwicoS1v12 swicoS1v12 = new SwicoS1v12();
        swicoS1v12.setInvoiceReference(invoiceReference);
        swicoS1v12.setInvoiceDate(invoiceDate);
        swicoS1v12.setCustomerReference(customerReference);
        swicoS1v12.setUidNumber(uidNumber);
        swicoS1v12.setVatDateStart(vatDateStart);
        swicoS1v12.setVatDateEnd(vatDateEnd);
        swicoS1v12.setVatDetails(vatDetails);
        swicoS1v12.setImportTaxes(importTaxes);
        swicoS1v12.setPaymentConditions(paymentConditions);
        return swicoS1v12;
    }
}
