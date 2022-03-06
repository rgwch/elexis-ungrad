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

import ch.codeblock.qrinvoice.model.AdditionalInformation;
import ch.codeblock.qrinvoice.model.PaymentReference;
import ch.codeblock.qrinvoice.model.ReferenceType;
import ch.codeblock.qrinvoice.util.CreditorReferenceUtils;
import ch.codeblock.qrinvoice.util.QRReferenceUtils;
import ch.codeblock.qrinvoice.util.ReferenceUtils;

import static ch.codeblock.qrinvoice.model.ReferenceType.*;

public final class PaymentReferenceBuilder {
    private ReferenceType referenceType;
    private String reference;
    private AdditionalInformation additionalInformation;

    private PaymentReferenceBuilder() {
    }

    public static PaymentReferenceBuilder create() {
        return new PaymentReferenceBuilder();
    }

    /**
     * @param referenceType type to set
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     */
    public PaymentReferenceBuilder referenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
        return this;
    }

    /*
     * @see PaymentReference#getReference
     * @param reference reference to set
     * @return PaymentReferenceBuilder instance
     */
    public PaymentReferenceBuilder reference(String reference) {
        this.reference = reference;
        return this;
    }

    /**
     * @param additionalInformation Additional Information to set
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getAdditionalInformation()
     */
    public PaymentReferenceBuilder additionalInformation(AdditionalInformation additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }

    /**
     * <pre>
     *     .qrReference("...")
     * </pre>
     * is the same as
     * <pre>
     *     .referenceType(ReferenceType.QR_REFERENCE)
     *     .reference("...")
     * </pre>
     *
     * @param reference QR reference to set
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     * @see PaymentReference#getReference()
     * @see ReferenceType#QR_REFERENCE
     */
    public PaymentReferenceBuilder qrReference(String reference) {
        return referenceType(QR_REFERENCE).reference(reference);
    }

    /**
     * <pre>
     *     .buildQrReference("...")
     * </pre>
     * is the same as
     * <pre>
     *     .referenceType(ReferenceType.QR_REFERENCE)
     *     .reference("...with shortened reference number...")
     * </pre>
     *
     * @param reference QR reference to set (May be shortened)
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     * @see PaymentReference#getReference()
     * @see ReferenceType#QR_REFERENCE
     */
    public PaymentReferenceBuilder buildQrReference(String reference) {
        return qrReference(QRReferenceUtils.createQrReference(reference));
    }

    /**
     * <pre>
     *     .buildQrReference("customerId", "...")
     * </pre>
     * is the same as
     * <pre>
     *     .referenceType(ReferenceType.QR_REFERENCE)
     *     .reference("...with customer Id and shortened reference number...")
     * </pre>
     *
     * @param customerId Customer Id to set
     * @param reference  QR reference to set (May be shortened)
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     * @see PaymentReference#getReference()
     * @see ReferenceType#QR_REFERENCE
     */
    public PaymentReferenceBuilder buildQrReference(String customerId, String reference) {
        return qrReference(QRReferenceUtils.createQrReference(customerId, reference));
    }

    /**
     * <pre>
     *     .creditorReference("...")
     * </pre>
     * is the same as
     * <pre>
     *     .referenceType(ReferenceType.CREDITOR_REFERENCE)
     *     .reference("...")
     * </pre>
     *
     * @param reference Creditor reference to set
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     * @see PaymentReference#getReference()
     * @see ReferenceType#CREDITOR_REFERENCE
     */
    public PaymentReferenceBuilder creditorReference(String reference) {
        return referenceType(CREDITOR_REFERENCE).reference(reference);
    }

    /**
     * <pre>
     *     .buildCreditorReference("...")
     * </pre>
     * is the same as
     * <pre>
     *     .referenceType(ReferenceType.CREDITOR_REFERENCE)
     *     .reference("...with shortened reference number...")
     * </pre>
     *
     * @param reference Creditor reference to set (May be shortened)
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     * @see PaymentReference#getReference()
     * @see ReferenceType#CREDITOR_REFERENCE
     */
    public PaymentReferenceBuilder buildCreditorReference(String reference) {
        return referenceType(CREDITOR_REFERENCE).reference(CreditorReferenceUtils.createCreditorReference(reference));
    }

    /**
     * <pre>
     *     .withoutReference()
     * </pre>
     * is the same as
     * <pre>
     *     .referenceType(ReferenceType.WITHOUT_REFERENCE)
     * </pre>
     *
     * @return PaymentReferenceBuilder instance
     * @see PaymentReference#getReferenceType()
     * @see ReferenceType#WITHOUT_REFERENCE
     */
    public PaymentReferenceBuilder withoutReference() {
        return referenceType(WITHOUT_REFERENCE);
    }

    /**
     * @return The created {@link PaymentReference} based on the data set to this builder
     */
    public PaymentReference build() {
        if (referenceType == null) {
            // referenceType is mandatory, but if no reference is provided, WITHOUT_REFERENCE makes total sense as a default
            referenceType = WITHOUT_REFERENCE;
        }
        PaymentReference paymentReference = new PaymentReference();
        paymentReference.setReferenceType(referenceType);
        paymentReference.setReference(ReferenceUtils.normalize(referenceType, reference));
        paymentReference.setAdditionalInformation(additionalInformation);
        return paymentReference;
    }
}
