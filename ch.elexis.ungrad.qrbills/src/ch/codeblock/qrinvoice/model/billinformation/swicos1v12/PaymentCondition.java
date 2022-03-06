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

public class PaymentCondition implements Comparable<PaymentCondition> {
    private int eligiblePaymentPeriodDays;

    public PaymentCondition(final BigDecimal cashDiscountPercentage, final int eligiblePaymentPeriodDays) {
        this.cashDiscountPercentage = cashDiscountPercentage;
        this.eligiblePaymentPeriodDays = eligiblePaymentPeriodDays;
    }

    /**
     * The cash discount percentage (Skonto-Prozentsatz)
     */
    @Example("2")
    @Description("See Tag 40 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public BigDecimal getCashDiscountPercentage() {
        return cashDiscountPercentage;
    }

    private BigDecimal cashDiscountPercentage;

    /**
     * In how many days starting from invoice date the amount must be paid in order to get the discount (Zahlungsfrist Tage zur Berechtigung des Skontoabzugs)
     */
    @Example("10")
    @Description("See Tag 40 of Swico Syntax Definition S1 v1.2 - http://swiss-qr-invoice.org/")
    public int getEligiblePaymentPeriodDays() {
        return eligiblePaymentPeriodDays;
    }

    public void setCashDiscountPercentage(final BigDecimal cashDiscountPercentage) {
        this.cashDiscountPercentage = cashDiscountPercentage;
    }

    public void setEligiblePaymentPeriodDays(final int eligiblePaymentPeriodDays) {
        this.eligiblePaymentPeriodDays = eligiblePaymentPeriodDays;
    }

    @Override
    public int compareTo(final PaymentCondition o) {
        return this.eligiblePaymentPeriodDays - o.eligiblePaymentPeriodDays;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PaymentCondition that = (PaymentCondition) o;
        return eligiblePaymentPeriodDays == that.eligiblePaymentPeriodDays &&
                Objects.equals(cashDiscountPercentage, that.cashDiscountPercentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cashDiscountPercentage, eligiblePaymentPeriodDays);
    }
}
