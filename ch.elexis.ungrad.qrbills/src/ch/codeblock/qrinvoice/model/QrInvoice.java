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

import ch.codeblock.qrinvoice.model.annotation.Mandatory;
import ch.codeblock.qrinvoice.model.annotation.Optional;

import java.util.Objects;

/**
 * <p>Swiss QR Invoice root element</p>
 * <p>Data Structure Element</p>
 * <pre>
 *     QRCH
 * </pre>
 */
public class QrInvoice {
    private Header header;
    private CreditorInformation creditorInformation;
    private UltimateCreditor ultimateCreditor;
    private PaymentAmountInformation paymentAmountInformation;
    private UltimateDebtor ultimateDebtor;
    private PaymentReference paymentReference;
    private AlternativeSchemes alternativeSchemes;

    @Mandatory
    public Header getHeader() {
        return header;
    }

    public void setHeader(final Header header) {
        this.header = header;
    }

    @Mandatory
    public CreditorInformation getCreditorInformation() {
        return creditorInformation;
    }

    public void setCreditorInformation(final CreditorInformation creditorInformation) {
        this.creditorInformation = creditorInformation;
    }

    @Optional
    public UltimateCreditor getUltimateCreditor() {
        return ultimateCreditor;
    }

    public void setUltimateCreditor(final UltimateCreditor ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }
    
    @Mandatory
    public PaymentAmountInformation getPaymentAmountInformation() {
        return paymentAmountInformation;
    }

    public void setPaymentAmountInformation(final PaymentAmountInformation paymentAmountInformation) {
        this.paymentAmountInformation = paymentAmountInformation;
    }

    @Optional
    public UltimateDebtor getUltimateDebtor() {
        return ultimateDebtor;
    }

    public void setUltimateDebtor(final UltimateDebtor ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }

    @Mandatory
    public PaymentReference getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(final PaymentReference paymentReference) {
        this.paymentReference = paymentReference;
    }

    @Optional
    public AlternativeSchemes getAlternativeSchemes() {
        return alternativeSchemes;
    }

    public void setAlternativeSchemes(final AlternativeSchemes alternativeSchemes) {
        this.alternativeSchemes = alternativeSchemes;
    }

    @Override
    public String toString() {
        return "QrInvoice{" +
                "header=" + header +
                ", creditorInformation=" + creditorInformation +
                ", ultimateCreditor=" + ultimateCreditor +
                ", paymentAmountInformation=" + paymentAmountInformation +
                ", ultimateDebtor=" + ultimateDebtor +
                ", paymentReference=" + paymentReference +
                ", alternativeSchemes=" + alternativeSchemes +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final QrInvoice qrInvoice = (QrInvoice) o;
        return Objects.equals(header, qrInvoice.header) &&
                Objects.equals(creditorInformation, qrInvoice.creditorInformation) &&
                Objects.equals(ultimateCreditor, qrInvoice.ultimateCreditor) &&
                Objects.equals(paymentAmountInformation, qrInvoice.paymentAmountInformation) &&
                Objects.equals(ultimateDebtor, qrInvoice.ultimateDebtor) &&
                Objects.equals(paymentReference, qrInvoice.paymentReference) &&
                Objects.equals(alternativeSchemes, qrInvoice.alternativeSchemes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, creditorInformation, ultimateCreditor, paymentAmountInformation, ultimateDebtor, paymentReference, alternativeSchemes);
    }
}
