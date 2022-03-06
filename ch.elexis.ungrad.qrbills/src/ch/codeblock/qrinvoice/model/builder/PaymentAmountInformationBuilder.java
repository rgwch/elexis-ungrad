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
package ch.codeblock.qrinvoice.model.builder;

import ch.codeblock.qrinvoice.model.PaymentAmountInformation;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;

import java.math.BigDecimal;
import java.util.Currency;

public final class PaymentAmountInformationBuilder {
    private BigDecimal amount;
    private Currency currency;

    private PaymentAmountInformationBuilder() {
    }

    public static PaymentAmountInformationBuilder create() {
        return new PaymentAmountInformationBuilder();
    }

    /**
     * @param amount The amount to set
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getAmount() 
     */
    public PaymentAmountInformationBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    /**
     * @param amount The amount to set
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getAmount() 
     */
    public PaymentAmountInformationBuilder amount(final double amount) {
        return amount(BigDecimal.valueOf(amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @param currency The currency to set
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Set a given amount in Swiss Francs
     * <pre>
     *     .chf(BigDecimal.valueOf(42.00))
     * </pre>
     * is the same as
     * <pre>
     *     .currency(Currency.getInstance("CHF"))
     *     .amount(BigDecimal.valueOf(42.00))
     * </pre>
     *
     * @param amount The amount to set in CHF
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getAmount() 
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder chf(final BigDecimal amount) {
        return currency(SwissPaymentsCode.CHF).amount(amount);
    }

    /**
     * Set a given amount in Swiss Francs
     * <pre>
     *     .chf(42.00d)
     * </pre>
     * is the same as
     * <pre>
     *     .currency(Currency.getInstance("CHF"))
     *     .amount(42.00d)
     * </pre>
     *
     * @param amount The amount to set in CHF
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getAmount() 
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder chf(final double amount) {
        return currency(SwissPaymentsCode.CHF).amount(amount);
    }


    /**
     * Set a given amount in Euro
     * <pre>
     *     .eur(BigDecimal.valueOf(42.00))
     * </pre>
     * is the same as
     * <pre>
     *     .currency(Currency.getInstance("EUR"))
     *     .amount(BigDecimal.valueOf(42.00))
     * </pre>
     *
     * @param amount The amount to set in EUR
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getAmount() 
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder eur(final BigDecimal amount) {
        return currency(SwissPaymentsCode.EUR).amount(amount);
    }

    /**
     * Set a given amount in Euro
     * <pre>
     *     .eur(42.00d)
     * </pre>
     * is the same as
     * <pre>
     *     .currency(Currency.getInstance("EUR"))
     *     .amount(42.00d)
     * </pre>
     *
     * @param amount The amount to set in EUR
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getAmount() 
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder eur(final double amount) {
        return currency(SwissPaymentsCode.EUR).amount(amount);
    }

    /**
     * Set a given amount in Euro
     * <pre>
     *     .eur()
     * </pre>
     * is the same as
     * <pre>
     *     .currency(Currency.getInstance("EUR"))
     * </pre>
     *
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder eur() {
        return currency(SwissPaymentsCode.EUR);
    }

    /**
     * Set a given amount in Euro
     * <pre>
     *     .chf()
     * </pre>
     * is the same as
     * <pre>
     *     .currency(Currency.getInstance("CHF"))
     * </pre>
     *
     * @return PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation#getCurrency() 
     */
    public PaymentAmountInformationBuilder chf() {
        return currency(SwissPaymentsCode.CHF);
    }

    public PaymentAmountInformation build() {
        PaymentAmountInformation paymentAmountInformation = new PaymentAmountInformation();
        paymentAmountInformation.setAmount(amount);
        paymentAmountInformation.setCurrency(currency);
        return paymentAmountInformation;
    }
}
