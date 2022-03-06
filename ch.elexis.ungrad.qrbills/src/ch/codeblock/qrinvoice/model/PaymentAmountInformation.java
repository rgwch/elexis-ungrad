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
package ch.codeblock.qrinvoice.model;

import ch.codeblock.qrinvoice.model.annotation.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * <p>From the specification v2.0</p>
 * <table border="1" summary="Excerpt from the specification">
 * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
 * <tr><td>EN</td><td>Payment amount information</td><td>Mandatory data group</td></tr>
 * <tr><td>DE</td><td>Zahlbetragsinformation</td><td>Obligatorische Datengruppe</td></tr>
 * <tr><td>FR</td><td>Information sur le montant du paiement</td><td>Groupe de données obligatoire</td></tr>
 * <tr><td>IT</td><td>Informazioni sull’importo da pagare</td><td>Gruppo di dati obbligatorio<br> </td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +CcyAmt
 * </pre>
 */
public class PaymentAmountInformation {
    private BigDecimal amount;
    private Currency currency;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Amount<br>The payment amount</td><td>The amount element is to be entered without leading zeroes, including decimal separators and two decimal places.<br><br>Decimal, maximum 12-digits permitted, including decimal separators. Only decimal points (".") are permitted as decimal separators.</td></tr>
     * <tr><td>DE</td><td>Betrag<br>Betrag der Zahlung</td><td>Das Element ist ohne führende Nullen, inklusive Dezimaltrenn zeichen und zwei Nachkommastellen, anzugeben.<br>Dezimal, max. 12 Stellen zulässig, inklusive Dezimaltrennzeichen.<br>Als Dezimaltrennzeichen ist nur das Punktzeichen («.») zulässig.</td></tr>
     * <tr><td>FR</td><td>Montant<br>Montant du paiement</td><td>L'élément est à indiquer sans zéros de tête y compris séparateur décimal et deux décimales.<br>Décimal, 12 positions au maximum admises, y compris séparateur décimal. Seul le point («.») est admis comme séparateur décimal.</td></tr>
     * <tr><td>IT</td><td>Importo<br>Importo del pagamento</td><td>L’elemento deve essere indicato senza zeri antecedenti, con il segno di separazione dei decimali e due cifre dopo la virgola.<br>Per i decimali, ammessi al massimo 12 caratteri, incluso il segno di separazione.<br>Come segno di separazione dei decimali<br>è ammesso il punto (.).</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +CcyAmt
     * ++Amt
     * </pre>
     */
    @Optional
    @Size(min = 0, max = 999999999)
    @QrchPath("CcyAmt/Amt")
    @Description("The payment amount<br>The amount element is to be entered without leading zeroes, including decimal separators and two decimal places.<br><br>Decimal, maximum 12-digits permitted, including decimal separators. Only decimal points (\".\") are permitted as decimal separators.")
    @Example("199.95")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Currency<br>The payment currency, 3-digit alphanumeric currency code according to ISO 4217</td><td>Only CHF and EUR are permitted.</td></tr>
     * <tr><td>DE</td><td>Währung<br>Währung der Zahlung, dreistelliger alphabetischer Währungscode gemäss ISO 4217</td><td>Nur CHF und EUR zugelassen.</td></tr>
     * <tr><td>FR</td><td>Monnaie<br>Monnaie du paiement, code monétaire alphabétique à trois positions selon ISO 4217</td><td>Seuls CHF et EUR sont admis.</td></tr>
     * <tr><td>IT</td><td>Valuta<br>Valuta del pagamento, codice valuta alfanumerico a 3 caratteri secondo ISO 4217</td><td>Supportati solo CHF e EUR.</td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +CcyAmt
     * ++Ccy
     * </pre>
     */
    @Mandatory
    @Size(min = 3, max = 3)
    @QrchPath("CcyAmt/Ccy")
    @Description("The payment currency, 3-digit alphanumeric currency code according to ISO 4217<br>Only CHF and EUR are permitted.")
    @Example("CHF")
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "PaymentAmountInformation{" +
                "amount=" + amount +
                ", currency=" + currency +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PaymentAmountInformation that = (PaymentAmountInformation) o;
        return Objects.equals(amount, that.amount) &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
