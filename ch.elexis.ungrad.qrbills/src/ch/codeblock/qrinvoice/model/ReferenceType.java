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

import ch.codeblock.qrinvoice.model.annotation.Description;

public enum ReferenceType {
    /**
     * <p>QRR – QR reference</p>
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>Description</th></tr>
     * <tr><td>EN</td><td>The biller's structured reference corresponds to the former ISR reference number.</td></tr>
     * <tr><td>DE</td><td>Die strukturierte Referenz des Rechnungsstellers entspricht der ehemaligen ESR Referenznummer.</td></tr>
     * <tr><td>FR</td><td>Référence structurée de l'émetteur de factures, correspondant à l'ancien numéro de référence BVR.</td></tr>
     * <tr><td>IT</td><td>Riferimento strutturato del mittente della fattura, corrispondente all’ex numero di riferimento della polizza di versamento.</td></tr>
     * </table>
     *
     * @see PaymentReference#getReferenceType() 
     */
    @Description("The biller's structured reference corresponds to the former ISR reference number.")
    QR_REFERENCE("QRR"),
    /**
     * <p>SCOR – Creditor Reference (ISO 11649)</p>
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>Description</th></tr>
     * <tr><td>EN</td><td>Creditor Reference according to the ISO 11649 standard.</td></tr>
     * <tr><td>DE</td><td>Creditor Reference gemäss ISO-11649-Standard.</td></tr>
     * <tr><td>FR</td><td>Creditor Reference selon la norme ISO 11649.</td></tr>
     * <tr><td>IT</td><td>Riferimento strutturato al creditore in conformità allo standard ISO 11649.</td></tr>
     * </table>
     *
     * @see PaymentReference#getReferenceType() 
     */
    @Description("Creditor Reference according to the ISO 11649 standard.")
    CREDITOR_REFERENCE("SCOR"),
    /**
     * <p>NON – without reference</p>
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>Description</th></tr>
     * <tr><td>EN</td><td></td></tr>
     * <tr><td>DE</td><td></td></tr>
     * <tr><td>FR</td><td></td></tr>
     * <tr><td>IT</td><td></td></tr>
     * </table>
     *
     * @see PaymentReference#getReferenceType() 
     */
    @Description("NON – without reference")
    WITHOUT_REFERENCE("NON");

    private final String referenceTypeCode;

    ReferenceType(final String referenceTypeCode) {
        this.referenceTypeCode = referenceTypeCode;
    }

    public String getReferenceTypeCode() {
        return referenceTypeCode;
    }

    public static ReferenceType parse(final String referenceTypeCode) {
        for (final ReferenceType referenceType : values()) {
            if (referenceType.getReferenceTypeCode().equals(referenceTypeCode)) {
                return referenceType;
            }
        }
        throw new ParseException("Invalid reference type '" + referenceTypeCode + "' given");
    }

    @Override
    public String toString() {
        return "ReferenceType{" +
                "referenceTypeCode='" + referenceTypeCode + '\'' +
                "} " + super.toString();
    }
}
