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

import ch.codeblock.qrinvoice.banner.Banner;
import ch.codeblock.qrinvoice.model.*;
import ch.codeblock.qrinvoice.model.validation.QrInvoiceValidator;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.util.CollectionUtils;
import ch.codeblock.qrinvoice.util.NumberUtils;
import ch.codeblock.qrinvoice.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * <p>Create always a new QrInvoiceBuilder instance in order to create a new QrInvoice</p>
 * <p>This builder is not thread safe on purpose.</p>
 */
public final class QrInvoiceBuilder {
    static {
        Banner.printBanner();
    }

    // Builder delegates
    private CreditorInformationBuilder creditorInformationBuilder;
    private CreditorBuilder creditorBuilder;
    private UltimateCreditorBuilder ultimateCreditorBuilder;
    private UltimateDebtorBuilder ultimateDebtorBuilder;
    private PaymentAmountInformationBuilder paymentAmountInformationBuilder;
    private PaymentReferenceBuilder paymentReferenceBuilder;
    private AdditionalInformationBuilder additionalInformationBuilder;
    private AlternativeSchemesBuilder alternativeSchemesBuilder;

    // CreditorInformation
    private String iban;

    // AlternativeSchemeParameters
    private List<String> alternativeSchemeParameters;

    private QrInvoiceBuilder() {
    }

    public static QrInvoiceBuilder create() {
        return new QrInvoiceBuilder();
    }

    /**
     * @return The CreditorInformationBuilder instance
     * @see CreditorInformation
     */
    private CreditorInformationBuilder creditorInformation() {
        if (creditorInformationBuilder == null) {
            creditorInformationBuilder = CreditorInformationBuilder.create();
        }
        return creditorInformationBuilder;
    }

    /**
     * @return The CreditorBuilder instance
     * @see Creditor
     */
    public AddressBuilder creditor() {
        if (creditorBuilder == null) {
            creditorBuilder = CreditorBuilder.create();
        }
        return creditorBuilder;
    }

    /**
     * @return The UltimateCreditorBuilder instance
     * @see UltimateCreditor
     */
    public AddressBuilder ultimateCreditor() {
        if (ultimateCreditorBuilder == null) {
            ultimateCreditorBuilder = UltimateCreditorBuilder.create();
        }
        return ultimateCreditorBuilder;
    }

    /**
     * @return The UltimateDebtorBuilder instance
     * @see UltimateDebtor
     */
    public AddressBuilder ultimateDebtor() {
        if (ultimateDebtorBuilder == null) {
            ultimateDebtorBuilder = UltimateDebtorBuilder.create();
        }
        return ultimateDebtorBuilder;
    }

    /**
     * @return The PaymentAmountInformationBuilder instance
     * @see PaymentAmountInformation
     */
    public PaymentAmountInformationBuilder paymentAmountInformation() {
        if (paymentAmountInformationBuilder == null) {
            paymentAmountInformationBuilder = PaymentAmountInformationBuilder.create();
        }
        return paymentAmountInformationBuilder;
    }

    /**
     * @return The PaymentReferenceBuilder instance
     * @see PaymentReference
     */
    public PaymentReferenceBuilder paymentReference() {
        if (paymentReferenceBuilder == null) {
            paymentReferenceBuilder = PaymentReferenceBuilder.create();
        }
        return paymentReferenceBuilder;
    }

    /**
     * @return The AdditionalInformationBuilder instance
     * @see AdditionalInformation
     */
    public AdditionalInformationBuilder additionalInformation() {
        if (additionalInformationBuilder == null) {
            additionalInformationBuilder = AdditionalInformationBuilder.create();
        }
        return additionalInformationBuilder;
    }

    /**
     * @return The AlternativeSchemesBuilder instance
     * @see AlternativeSchemes
     */
    private AlternativeSchemesBuilder alternativeSchemes() {
        if (alternativeSchemesBuilder == null) {
            alternativeSchemesBuilder = AlternativeSchemesBuilder.create();
        }
        return alternativeSchemesBuilder;
    }

    /**
     * @param func A lambda function which uses the supplied {@link CreditorBuilder} to build the {@link Creditor}
     * @return The {@link QrInvoiceBuilder} instance
     * @see Creditor
     */
    public QrInvoiceBuilder creditor(final Consumer<AddressBuilder> func) {
        func.accept(creditor());
        return this;
    }

    /**
     * @param func A lambda function which uses the supplied {@link UltimateCreditorBuilder} to build the {@link UltimateCreditor}
     * @return The {@link QrInvoiceBuilder} instance
     * @see UltimateCreditor
     */
    public QrInvoiceBuilder ultimateCreditor(final Consumer<AddressBuilder> func) {
        func.accept(ultimateCreditor());
        return this;
    }

    /**
     * @param func A lambda function which uses the supplied {@link UltimateDebtorBuilder} to build the {@link UltimateDebtor}
     * @return The {@link QrInvoiceBuilder} instance
     * @see UltimateDebtor
     */
    public QrInvoiceBuilder ultimateDebtor(final Consumer<AddressBuilder> func) {
        func.accept(ultimateDebtor());
        return this;
    }

    /**
     * @param func A lambda function which uses the supplied {@link PaymentAmountInformationBuilder} to build the {@link PaymentAmountInformation}
     * @return The {@link QrInvoiceBuilder} instance
     * @see PaymentAmountInformation
     */
    public QrInvoiceBuilder paymentAmountInformation(final Consumer<PaymentAmountInformationBuilder> func) {
        func.accept(paymentAmountInformation());
        return this;
    }

    /**
     * @param func A lambda function which uses the supplied {@link PaymentReferenceBuilder} to build the {@link PaymentReference}
     * @return The {@link QrInvoiceBuilder} instance
     * @see PaymentReference
     */
    public QrInvoiceBuilder paymentReference(final Consumer<PaymentReferenceBuilder> func) {
        func.accept(paymentReference());
        return this;
    }

    /**
     * @param func A lambda function which uses the supplied {@link AdditionalInformationBuilder} to build the {@link AdditionalInformation}
     * @return The {@link QrInvoiceBuilder} instance
     * @see AdditionalInformation
     */
    public QrInvoiceBuilder additionalInformation(final Consumer<AdditionalInformationBuilder> func) {
        func.accept(additionalInformation());
        return this;
    }

    /**
     * @param iban The creditors IBAN. Whitespaces will be removed in {@link #build()}
     * @return The {@link QrInvoiceBuilder} instance
     * @see CreditorInformation#getIban()
     */
    public QrInvoiceBuilder creditorIBAN(final String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * @param alternativeSchemeParameters A list of alternative schema parameter strings to set
     * @return The {@link QrInvoiceBuilder} instance
     * @see AlternativeSchemes
     */
    public QrInvoiceBuilder alternativeSchemeParameters(final List<String> alternativeSchemeParameters) {
        this.alternativeSchemeParameters = alternativeSchemeParameters;
        return this;
    }

    public QrInvoiceBuilder doNotUseForPayment() throws ValidationException {
        return doNotUseForPayment(Locale.GERMAN);
    }

    public QrInvoiceBuilder doNotUseForPayment(Locale locale) throws ValidationException {
        final BigDecimal amount = this.paymentAmountInformation().getAmount();
        if (amount == null || NumberUtils.isZero(amount)) {
            this.paymentAmountInformation().chf(DoNotUseForPayment.ZERO_AMOUNT);
        } else {
            throw ValidationException.fromMessageKey("validation.error.doNotUseForPayment.nonZeroAmount");
        }

        if (StringUtils.isBlank(this.additionalInformation().getUnstructuredMessage()) || DoNotUseForPayment.UNSTRUCTURED_MESSAGES.contains(this.additionalInformation().getUnstructuredMessage())) {
            this.additionalInformation().unstructuredMessage(DoNotUseForPayment.getUnstructuredMessage(locale));
        } else {
            throw ValidationException.fromMessageKey("validation.error.doNotUseForPayment.nonEmptyUnstructuredMessage");
        }

        return this;
    }

    /**
     * @return the {@link QrInvoice}
     * @throws ValidationException if given data is not valid from a QR-Invoice perspective
     */
    public QrInvoice build() throws ValidationException {
        // create child elements
        final Header header = buildHeader();
        final CreditorInformation creditorInformation = buildCreditorInformation();
        final UltimateCreditor ultimateCreditor = buildUltimateCreditor();
        final UltimateDebtor ultimateDebtor = buildUltimateDebtor();
        final PaymentAmountInformation paymentAmountInformation = buildPaymentAmountInformation();
        final PaymentReference paymentReference = buildPaymentReference();
        final AlternativeSchemes alternativeSchemes = buildAlternativeSchemes();

        // Do not use for payment / notification
        if (paymentAmountInformation.getAmount() != null && NumberUtils.isZero(paymentAmountInformation.getAmount()) &&
                StringUtils.isBlank(paymentReference.getAdditionalInformation().getUnstructuredMessage())) {
            paymentReference.getAdditionalInformation().setUnstructuredMessage(DoNotUseForPayment.getUnstructuredMessage(Locale.GERMAN));
        }

        // build QrInvoice root object
        final QrInvoice qrInvoice = new QrInvoice();
        qrInvoice.setHeader(header);
        qrInvoice.setCreditorInformation(creditorInformation);
        qrInvoice.setUltimateCreditor(ultimateCreditor);
        qrInvoice.setUltimateDebtor(ultimateDebtor);
        qrInvoice.setPaymentAmountInformation(paymentAmountInformation);
        qrInvoice.setPaymentReference(paymentReference);
        qrInvoice.setAlternativeSchemes(alternativeSchemes);

        // perform validation
        QrInvoiceValidator.create().validate(qrInvoice).throwExceptionOnErrors();

        return qrInvoice;
    }

    private Header buildHeader() {
        return HeaderBuilder.create()
                .qrType(SwissPaymentsCode.QR_TYPE)
                .version(Short.parseShort(SwissPaymentsCode.SPC_VERSION))
                .codingType(SwissPaymentsCode.CODING_TYPE).build();
    }

    private CreditorInformation buildCreditorInformation() {
        final CreditorInformationBuilder creditorInformationBuilder = creditorInformation();
        if (creditorBuilder != null) {
            creditorInformationBuilder.creditor(creditorBuilder.build());
        }
        return creditorInformationBuilder.iban(iban)
                .build();
    }

    private UltimateCreditor buildUltimateCreditor() {
        final UltimateCreditor ultimateCreditor;
        if (ultimateCreditorBuilder != null) {
            ultimateCreditor = ultimateCreditorBuilder.build();
        } else {
            ultimateCreditor = null;
        }
        return ultimateCreditor;
    }

    private UltimateDebtor buildUltimateDebtor() {
        final UltimateDebtor ultimateDebtor;
        if (ultimateDebtorBuilder != null) {
            ultimateDebtor = ultimateDebtorBuilder.build();
        } else {
            ultimateDebtor = null;
        }
        return ultimateDebtor;
    }

    private PaymentAmountInformation buildPaymentAmountInformation() {
        return paymentAmountInformation().build();
    }

    private PaymentReference buildPaymentReference() {
        final AdditionalInformation additionalInformation = additionalInformation()
                .trailer(SwissPaymentsCode.END_PAYMENT_DATA_TRAILER)
                .build();
        return paymentReference()
                .additionalInformation(additionalInformation)
                .build();
    }

    private AlternativeSchemes buildAlternativeSchemes() {
        // optional group
        if (CollectionUtils.isNotEmpty(alternativeSchemeParameters)) {
            alternativeSchemes().alternativeSchemeParameters(alternativeSchemeParameters);
        }
        if (alternativeSchemesBuilder != null) {
            return alternativeSchemesBuilder.build();
        } else {
            return null;
        }
    }

}
