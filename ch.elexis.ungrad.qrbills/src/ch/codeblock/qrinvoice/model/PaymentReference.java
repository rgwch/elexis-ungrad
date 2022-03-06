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

import java.util.Objects;

/**
 * <p>From the specification v2.0</p>
 * <table border="1" summary="Excerpt from the specification">
 * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
 * <tr><td>EN</td><td>Payment reference</td><td>Mandatory data group</td></tr>
 * <tr><td>DE</td><td>Zahlungsreferenz</td><td>Obligatorische Datengruppe</td></tr>
 * <tr><td>FR</td><td>Référence de paiement</td><td>Groupe de données obligatoire</td></tr>
 * <tr><td>IT</td><td>Riferimento dl pagamento</td><td>Gruppo di dati obbligatorio</td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +RmtInf
 * </pre>
 */
public class PaymentReference {
    private ReferenceType referenceType;
    private String reference;
    private AdditionalInformation additionalInformation;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Reference type<br>Reference type (QR, ISO)<br>The following codes are permitted:<br>QRR – QR reference<br>SCOR – Creditor Reference (ISO 11649)<br>NON – without reference</td><td>Maximum four characters, alphanumeric<br>Must contain the code QRR where a QR-IBAN is used;<br>where the IBAN is used, either the SCOR or NON code can be entered</td></tr>
     * <tr><td>DE</td><td>Referenztyp<br>Referenztyp (QR, ISO)<br>Die folgenden Codes sind zugelassen:<br>QRR – QR-Referenz<br>SCOR – Creditor Reference (ISO 11649)<br>NON – ohne Referenz</td><td>Maximal vier Zeichen, alphanumerisch;<br>Muss bei Verwendung einer QR-IBAN den Code QRR enthalten;<br>bei Verwendung der IBAN kann entweder der Code SCOR oder NON angegeben werden</td></tr>
     * <tr><td>FR</td><td>Type de référence<br>Type de référence (QR, ISO)<br>Les codes suivants sont admis:<br>QRR – Référence QR<br>SCOR – Creditor Reference (ISO 11649) NON – sans référence</td><td>Quatre caractères au maximum; En cas d'utilisation d'un QR-IBAN, doit contenir le code QRR;<br>en cas d'utilisation de l'IBAN, il est possible d'indiquer soit le code SCOR, soit le code NON.</td></tr>
     * <tr><td>IT</td><td>Tipo di riferimento<br>Tipo di riferimento (QR, ISO)<br>Sono ammessi i seguenti codici:<br>QRR – Riferimento-QR<br>SCOR – Creditor Reference (ISO 11649) NON – Senza riferimento</td><td>Massimo quattro caratteri, alfanumerici<br></td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +RmtInf
     * ++Tp
     * </pre>
     *
     * @see ReferenceType#QR_REFERENCE
     * @see ReferenceType#CREDITOR_REFERENCE
     * @see ReferenceType#WITHOUT_REFERENCE
     */
    @Mandatory
    @Size(min = 3, max = 4)
    @QrchPath("RmtInf/Tp")
    @Description("Reference type (QR, ISO)<br>The following codes are permitted:<br>QRR – QR reference<br>SCOR – Creditor Reference (ISO 11649)<br>NON – without reference<br>Maximum four characters, alphanumeric<br>Must contain the code QRR where a QR-IBAN is used;<br>where the IBAN is used, either the SCOR or NON code can be entered")
    @Example("SCOR")
    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(final ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Reference<br>Reference number<br>Structured payment reference<br>Note: The reference is either a QR reference or a Creditor Reference (ISO 11649)</td><td>Maximum 27 characters, alphanumeric;<br>must be filled if a QR-IBAN is used.<br>QR reference: 27 characters, numeric, check sum calculation according to Modulo 10 recursive (27th position of the reference)<br>Creditor Reference (ISO 11649): max 25 characters, alphanumeric<br>The element may not be filled for the NON reference type</td></tr>
     * <tr><td>DE</td><td>Referenz<br>Referenznummer<br>Strukturierte Zahlungsreferenz<br>Anmerkung: Die Referenz ist entweder eine QR-Referenz oder Creditor Reference (ISO 11649)</td><td>Maximal 27 Zeichen, alphanumerisch;<br>Muss bei Verwendung einer QR-IBAN befüllt werden.<br>QR-Referenz: 27 Zeichen, numerisch, Prüfzifferberechnung nach Modulo 10 rekursiv (27. Stelle der Referenz)<br>Creditor Reference (ISO 11649): bis 25 Zeichen, alphanumerisch.<br>Für den Referenztyp NON darf das Element nicht befüllt werden.</td></tr>
     * <tr><td>FR</td><td>Référence<br>Numéro de référence<br>Référence de paiement structurée<br>Remarque: La référence est soit une référence QR, soit une Creditor Reference (ISO 11649)</td><td>27 caractères alphanumériques au maximum; doit être rempli en cas d'utilisation d'un QR-IBAN.<br>Référence QR: 27 caractères numériques, calcul du chiffre de contrôle selon modulo 10 récursif (27e position de la référence).<br>Creditor Reference (ISO 11649): jusqu'à 25 caractères alphanumériques.<br>L'élément ne doit pas être rempli pour le type de référence NON.</td></tr>
     * <tr><td>IT</td><td>Riferimento<br>Numero di riferimento<br>Riferimento strutturato del pagamento<br>Nota: il riferimento consiste in un riferimento-QR o ISO Creditor Reference (ISO 11649)</td><td>Massimo 27 caratteri, alfanumerici. In caso di utilizzo di un QR- IBAN deve esser compilato.<br>Riferimento-QR: 27 caratteri, numerici, calcolo della check digit secondo il modulo 10 ricorsivo (27° carattere del riferimento) SCOR – Creditor Reference (ISO 11649): fino a 25 caratteri, alfanumerici<br>Per il tipo di riferimento NON, l’elemento non deve essere compilato.</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +RmtInf
     * ++Ref
     * </pre>
     */
    @Optional
    @Size(min = 0, max = 27)
    @QrchPath("RmtInf/Ref")
    @Description("Reference number<br>Structured payment reference<br>Note: The reference is either a QR reference or a Creditor Reference (ISO 11649)<br>Maximum 27 characters, alphanumeric;<br>must be filled if a QR-IBAN is used.<br>QR reference: 27 characters, numeric, check sum calculation according to Modulo 10 recursive (27th position of the reference)<br>Creditor Reference (ISO 11649): max 25 characters, alphanumeric<br>The element may not be filled for the NON reference type")
    @Example("RF18539007547034")
    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public AdditionalInformation getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(final AdditionalInformation additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    @Override
    public String toString() {
        return "PaymentReference{" +
                "referenceType=" + referenceType +
                ", reference='" + reference + '\'' +
                ", additionalInformation='" + additionalInformation + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PaymentReference that = (PaymentReference) o;
        return referenceType == that.referenceType &&
                Objects.equals(reference, that.reference) &&
                Objects.equals(additionalInformation, that.additionalInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceType, reference, additionalInformation);
    }
}
