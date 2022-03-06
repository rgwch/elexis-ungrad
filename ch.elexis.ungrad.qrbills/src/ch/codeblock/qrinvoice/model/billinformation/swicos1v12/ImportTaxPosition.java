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

import java.math.BigDecimal;
import java.util.Objects;

public class ImportTaxPosition {
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;

    public ImportTaxPosition(final BigDecimal taxPercentage, final BigDecimal taxAmount) {
        this.taxPercentage = taxPercentage;
        this.taxAmount = taxAmount;
    }

    /**
     * The import tax rate, e.g. 7.7% - represented as 7.7, not 0.077
     */
    @Example("7.7")
    @Description("See Tag 33 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    /**
     * The import tax amount (net tax amount, not the taxed amount)
     */
    @Example("1000")
    @Description("See Tag 33 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxPercentage(final BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public void setTaxAmount(final BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ImportTaxPosition that = (ImportTaxPosition) o;
        return Objects.equals(taxPercentage, that.taxPercentage) &&
                Objects.equals(taxAmount, that.taxAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxPercentage, taxAmount);
    }
}
