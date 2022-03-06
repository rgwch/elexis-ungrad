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
 * <tr><td>EN</td><td>Header<br>Header data. Contains basic information about the Swiss QR Code</td><td>Mandatory data group</td></tr>
 * <tr><td>DE</td><td>Header<br>Header-Daten. Enthält grundlegende Informationen über den QR-Code</td><td>Obligatorische Datengruppe</td></tr>
 * <tr><td>FR</td><td>Header<br>Données d'en-tête. Contient des informations fondamentales sur le code QR</td><td>Groupe de données obligatoire</td></tr>
 * <tr><td>IT</td><td>Header<br>Dati Header. Contiene informazioni essenziali sul codice QR</td><td>Gruppo di dati obbligatorio</td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +Header
 * </pre>
 */
public class Header {
    private String qrType;
    private short version;
    private byte codingType;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>QR Type<br>Unambiguous indicator for the Swiss QR Code. Fixed value “SPC” (Swiss Payments Code)</td><td>Fixed length: three-digit, alphanumeric</td></tr>
     * <tr><td>DE</td><td>QRType<br>Eindeutiges Kennzeichen für den Swiss QR Code. Fixer Wert «SPC» (Swiss Payments Code)</td><td>Feste Länge: dreistellig alphanumerisch</td></tr>
     * <tr><td>FR</td><td>QRType<br>Indicateur distinct pour le Swiss QR Code. Valeur fixe «SPC» (Swiss Payments Code)</td><td>Longueur fixe: trois positions alphanumériques</td></tr>
     * <tr><td>IT</td><td>QRType<br>Contrassegno univoco per il Swiss QR Code. Valore fisso «SPC» (Swiss Payments Code)</td><td>Lunghezza fissa: tre caratteri, alfanumerici</td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +Header
     * ++QRType
     * </pre>
     */
    @Mandatory
    @Size(min = 3, max = 3)
    @QrchPath("Header/QRType")
    @Description("Unambiguous indicator for the Swiss QR Code. Fixed value \"SPC\" (Swiss Payments Code)<br>Fixed length: three-digit, alphanumeric")
    @Example("SPC")
    public String getQrType() {
        return qrType;
    }

    public void setQrType(final String qrType) {
        this.qrType = qrType;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Version<br>Contains version of the specifications (Implementation Guidelines) in use on the date on which the Swiss QR Code was created. The first two positions indicate the main version, the following two positions the sub-version. Fixed value of "0200" for Version 2.0</td><td>Fixed length: four-digit, numeric</td></tr>
     * <tr><td>DE</td><td>Version<br>Beinhaltet die zum Zeitpunkt der Swiss-QR-Code-Erstellung verwendete Version der Spezifikation (Implementation Guidelines). Die ersten beiden Stellen bezeichnen die Hauptversion, die folgenden beiden Stellen die Unterversion. Fester Wert «0200» für Version 2.0.</td><td>Feste Länge: vierstellig numerisch</td></tr>
     * <tr><td>FR</td><td>Version<br>Contient la version de la spécification (Implementation Guidelines) utilisée au moment de la création du Swiss QR Code. Les deux premières positions indi- quent la version principale, les deux positions suivantes la sous-version. Valeur fixe «0200» pour la version 2.0</td><td>Longueur fixe: quatre positions numériques</td></tr>
     * <tr><td>IT</td><td>Versione<br>Contiene la versione della specifica (IG) utilizzata al momento della creazione del Swiss QR Code (Linee guida per l'implementazione). Le prime due cifre definiscono la versione principale, mentre le due successive la sotto-versione. Valore fisso «0200» per la versione 2.0</td><td>Lunghezza fissa: quattro caratteri, numerici</td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +Header
     * ++Version
     * </pre>
     */
    @Mandatory
    @Size(min = 0, max = 9999)
    @QrchPath("Header/Version")
    @Description("Contains version of the specifications (Implementation Guidelines) in use on the date on which the Swiss QR Code was created. The first two positions indicate the main version, the following two positions the sub-version. Fixed value of \"0200\" for Version 1.0<br>Fixed length: four-digit, numeric")
    @Example("0200")
    public short getVersion() {
        return version;
    }

    public void setVersion(final short version) {
        this.version = version;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Coding type<br>Character set code. Fixed value 1 (indicates Latin character set)</td><td>One-digit, numeric</td></tr>
     * <tr><td>DE</td><td>Coding Type<br>Zeichensatz-Code. Fixer Wert 1 (kennzeichnet Latin Character Set)</td><td>Einstellig numerisch</td></tr>
     * <tr><td>FR</td><td>Coding Type<br>Code de jeu de caractères. Valeur fixe 1 (désigne le «Latin Character Set»)</td><td>Une position numérique</td></tr>
     * <tr><td>IT</td><td>Coding Type<br>Codice del set di caratteri. Valore fisso 1 (identifica il Latin CharacterSet)</td><td>Un carattere, numerico</td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +Header
     * ++Coding
     * </pre>
     */
    @Mandatory
    @Size(min = 0, max = 9)
    @QrchPath("Header/Coding")
    @Description("Character set code. Fixed value 1 (indicates Latin character set)<br>One-digit, numeric")
    @Example("1")
    public byte getCodingType() {
        return codingType;
    }

    public void setCodingType(final byte codingType) {
        this.codingType = codingType;
    }

    @Override
    public String toString() {
        return "Header{" +
                "qrType='" + qrType + '\'' +
                ", version=" + version +
                ", codingType=" + codingType +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Header header = (Header) o;
        return version == header.version &&
                codingType == header.codingType &&
                Objects.equals(qrType, header.qrType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qrType, version, codingType);
    }

}
