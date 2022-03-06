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

import ch.codeblock.qrinvoice.model.annotation.Description;
import ch.codeblock.qrinvoice.model.annotation.Example;
import ch.codeblock.qrinvoice.model.validation.ValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Added Tax<br>
 * Die MWST-Details beziehen sich auf den Betrag der Rechnung, ohne Skonto.
 */
public class VatDetails {
    private BigDecimal taxPercentage;
    private BigDecimal taxedNetAmount;

    public VatDetails(final BigDecimal taxPercentage, final BigDecimal taxedNetAmount) {
        this.taxPercentage = taxPercentage;
        this.taxedNetAmount = taxedNetAmount;
    }

    public VatDetails(final BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
        this.taxedNetAmount = null;
    }

    /**
     * The VAT rate, e.g. 7.7% - represented as 7.7, not 0.077
     */
    @Example("7.7")
    @Description("See Tag 32 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    /**
     * The Net Amount (amount without VAT)
     */
    @Example("185.65")
    @Description("See Tag 32 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public BigDecimal getTaxedNetAmount() {
        return taxedNetAmount;
    }

    public boolean hasNetAmount() {
        return taxedNetAmount != null;
    }

    public void setTaxedNetAmount(final BigDecimal taxedNetAmount) {
        this.taxedNetAmount = taxedNetAmount;
    }

    public void setTaxPercentage(final BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public BigDecimal calculateGrossAmount() {
        if (taxedNetAmount == null) {
            throw new ValidationException("Unable to calculate gross amount as taxedNetAmount is missing");
        } else if (taxPercentage == null) {
            throw new ValidationException("Unable to calculate gross amount as taxPercentage is missing");
        }

        final BigDecimal vat = taxedNetAmount.multiply(taxPercentage).divide(BigDecimal.valueOf(100.0));
        return taxedNetAmount.add(vat); // gross amount
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final VatDetails that = (VatDetails) o;
        return Objects.equals(taxPercentage, that.taxPercentage) &&
                Objects.equals(taxedNetAmount, that.taxedNetAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxPercentage, taxedNetAmount);
    }
}
