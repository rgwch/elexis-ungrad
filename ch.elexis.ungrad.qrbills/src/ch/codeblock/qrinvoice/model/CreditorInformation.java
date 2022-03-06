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
 * <tr><td>EN</td><td>Creditor information</td><td>Mandatory data group</td></tr>
 * <tr><td>DE</td><td>Zahlungsempfänger Informationen</td><td>Obligatorische Datengruppe</td></tr>
 * <tr><td>FR</td><td>Informations sur le bénéficiaire</td><td>Groupe de données obligatoire</td></tr>
 * <tr><td>IT</td><td>Informazioni beneficiario</td><td>Gruppo di dati obbligatorio</td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +CdtrInf
 * </pre>
 */
public class CreditorInformation {
    private String iban;
    private Creditor creditor;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>IBAN<br>Account<br>IBAN or QR-IBAN of the creditor.</td><td>Fixed length: 21 alphanumeric characters, only IBANs with CH or LI country code permitted.</td></tr>
     * <tr><td>DE</td><td>IBAN<br>Konto<br>IBAN bzw. QR-IBAN des Zahlungsempfängers</td><td>Feste Länge: 21 alphanumerische Zeichen, nur IBANs mit CH- oder LI-Landescode zulässig.</td></tr>
     * <tr><td>FR</td><td>IBAN<br>Compte<br>IBAN ou QR-IBAN du bénéficiare</td><td>Longueur fixe: 21 caractères alphanumériques, IBAN seulement admis avec code de pays CH ou LI.</td></tr>
     * <tr><td>IT</td><td>IBAN<br>Conto<br>IBAN o QR-IBAN del beneficiario</td><td>Lunghezza fissa: 21 caratteri alfanumerici, ammessi solo IBAN con codice nazione CH- o LI.</td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +CdtrInf
     * ++IBAN
     * </pre>
     */
    @Mandatory
    @Size(min = 21, max = 21)
    @Description("IBAN or QR-IBAN of the creditor.<br>Fixed length: 21 alphanumeric characters, only IBANs with CH or LI country code permitted.")
    @Example("CH3908704016075473007")
    @QrchPath("CdtrInf/IBAN")
    public String getIban() {
        return iban;
    }

    public void setIban(final String iban) {
        this.iban = iban;
    }

    @Mandatory
    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(final Creditor creditor) {
        this.creditor = creditor;
    }

    @Override
    public String toString() {
        return "CreditorInformation{" +
                "iban='" + iban + '\'' +
                ", creditor=" + creditor +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CreditorInformation that = (CreditorInformation) o;
        return Objects.equals(iban, that.iban) &&
                Objects.equals(creditor, that.creditor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban, creditor);
    }
}
