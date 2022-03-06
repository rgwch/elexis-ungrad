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
import ch.codeblock.qrinvoice.model.util.AddressUtils;

import java.util.Objects;

/**
 * <p>From the specification v2.0</p>
 * <table border="1" summary="Excerpt from the specification">
 * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
 * <tr><td>EN</td><td>Ultimate creditor<br>Ultimate creditor<br>Information about the ultimate creditor</td><td>Optional data group; may only be used in agreement with the creditor’s financial institution</td></tr>
 * <tr><td>DE</td><td>Endgültiger Zahlungsempfänger<br>Endgültiger Zahlungsempfänger<br>Informationen zum endgültigen Zahlungsempfänger</td><td>Optionale Datengruppe; darf nur in Absprache mit dem Finanzinstitut des Zahlungsempfängers verwendet werden</td></tr>
 * <tr><td>FR</td><td>Bénéficiaire final<br>Bénéficiaire final<br>Informations sur le bénéficiaire final</td><td>Groupe de données optionnel; ne peut être utilisé que d'entente avec l'établissement financier du bénéficiaire</td></tr>
 * <tr><td>IT</td><td>Beneficiario finale<br>Beneficiario finale<br>Informazioni sul beneficiario finale</td><td>Gruppo di dati opzionale<br>Può essere usato solo in accordo con l’istituto finanziario del beneficiario.</td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +UltmtCdtr
 * </pre>
 */
public class UltimateCreditor implements Address {
    private AddressType addressType;
    private String name;

    // structured
    private String streetName;
    private String houseNumber;
    private String postalCode;
    private String city;

    // combined
    private String addressLine1;
    private String addressLine2;

    private String country;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Address type<br>The address type is specified using a code. The following codes are defined:<br>"S" - structured address<br>"K" - combined address elements (2 lines)</td><td>Fixed length: one-digit, alphanumeric</td></tr>
     * <tr><td>DE</td><td>Adress-Typ<br>Der Adress-Typ wird mittels eines Codes spezifiziert.<br>Folgende Codes sind definiert:<br>"S" - Strukturierte Adresse<br>"K" - Kombinierte Adressfelder (2 Zeilen)</td><td>Feste Länge: 1-stellig, alphanumerisch</td></tr>
     * <tr><td>FR</td><td>Type d'adresse<br>Le type d'adresse est spécifié à l'aide d'un code. Les codes suivants sont définis:<br>"S" - Adresse structurée<br>"K" - Champs d'adresse combinés (2 lignes)</td><td>Longueur fixe: une position alphanumérique</td></tr>
     * <tr><td>IT</td><td></td><td></td></tr>
     * </table>
     * <p>Status: {@link Mandatory}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++AdrTp
     * </pre>
     */
    @Override
    @Mandatory
    @Size(min = 1, max = 1)
    @QrchPath("UltmtCdtr/AdrTp")
    @Description("Address type<br>The address type is specified using a code. The following codes are defined:<br>\"S\" - structured address<br>\"K\" - combined address elements (2 lines)")
    @Example("STRUCTURED")
    public AddressType getAddressType() {
        return addressType;
    }

    @Override
    public void setAddressType(final AddressType addressType) {
        this.addressType = addressType;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Name<br>Name or company of the ultimate creditor</td><td>Maximum 70 characters permitted. first name (optional, if available) and last name or company name.</td></tr>
     * <tr><td>DE</td><td>Name<br>Name bzw. Firma des endgültigen Zahlungsempfängers</td><td>Maximal 70 Zeichen zulässig; Vorname (optional, falls verfügbar) und Name oder Firmenbezeichnung</td></tr>
     * <tr><td>FR</td><td>Nom<br>Nom ou entreprise du bénéficiaire final</td><td>70 caractères au maximum; prénom (optionnel, si disponible) et nom ou raison sociale</td></tr>
     * <tr><td>IT</td><td>Nome<br>Nome o azienda del beneficiario finale</td><td>Ammessi massimo 70 caratteri<br>Nome (opzionale, se disponibile) + cognome o ragione sociale</td></tr>
     * </table>
     * <p>Status: {@link Dependent}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++Name
     * </pre>
     */
    @Override
    @Dependent
    @Size(min = 1, max = 70)
    @QrchPath("UltmtCdtr/Name")
    @Description("Name or company of the ultimate creditor<br>Maximum 70 characters permitted. first name (optional, if available) and last name or company name.")
    @Example("Robert Schneider Services Switzerland AG")
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Street<br>Street/P.O. box of the ultimate creditor</td><td>Maximum 70 characters permitted; may not include any house or building number.</td></tr>
     * <tr><td>DE</td><td>Strasse<br>Strasse/Postfach des endgültigen Zahlungsempfängers</td><td>Maximal 70 Zeichen zulässig; darf keine Haus- bzw. Gebäudenummer enthalten.</td></tr>
     * <tr><td>FR</td><td>Rue<br>Rue/Case postale du bénéficiaire final</td><td>70 caractères au maximum admis; ne peut pas contenir un numéro de maison ou de bâtiment.</td></tr>
     * <tr><td>IT</td><td>Via<br>Via/casella postale dell’indirizzo del beneficiario finale</td><td>Ammessi massimo 70 caratteri; non deve contenere numeri civici.</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++StrtNmOrAdrLine1
     * </pre>
     */
    @Override
    @Optional
    @Size(min = 1, max = 70)
    @QrchPath("UltmtCdtr/StrtNmOrAdrLine1")
    @Description("Street/P.O. box of the ultimate creditor<br>Maximum 70 characters permitted; may not include any house or building number.")
    @Example("Rue du Lac")
    public String getStreetName() {
        return streetName;
    }

    @Override
    public void setStreetName(final String streetName) {
        this.streetName = streetName;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>House number<br>House number of the ultimate creditor</td><td>Maximum 16 characters permitted</td></tr>
     * <tr><td>DE</td><td>Hausnummer<br>Hausnummer des endgültigen Zahlungsempfängers</td><td>Maximal 16 Zeichen zulässig</td></tr>
     * <tr><td>FR</td><td>Numéro de maison<br>Numéro de maison du bénéficiaire final</td><td>16 caractères au maximum admis</td></tr>
     * <tr><td>IT</td><td>Numero civico<br>Numero civico dell’indirizzo del beneficiario finale</td><td>Ammessi massimo 16 caratteri</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++BldgNbOrAdrLine2
     * </pre>
     */
    @Optional
    @Size(min = 1, max = 16)
    @QrchPath("UltmtCdtr/BldgNbOrAdrLine2")
    @Description("House number of the ultimate creditor<br>Maximum 16 characters permitted")
    @Example("1268/3/1")
    @Override
    public String getHouseNumber() {
        return houseNumber;
    }

    @Override
    public void setHouseNumber(final String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Postal code<br>Postal code of the ultimate creditor</td><td>Maximum 16 characters permitted; is always to be entered without a country code prefix.</td></tr>
     * <tr><td>DE</td><td>Postleitzahl<br>Postleitzahl des endgültigen Zahlungsempfängers</td><td>Maximal 16 Zeichen zulässig; ist immer ohne vorangestellten Landescode anzugeben.</td></tr>
     * <tr><td>FR</td><td>Code postal<br>Code postal du bénéficiaire final</td><td>16 caractères au maximum admis; toujours à indiquer sans code de pays de tête.</td></tr>
     * <tr><td>IT</td><td>Numero postale di avviamento<br>Numero postale di avviamento dell’indirizzo del beneficiario finale</td><td>Ammessi massimo 16 caratteri; indicare sempre il numero postale di avviamento senza anteporre la sigla della nazione.</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++PstCd
     * </pre>
     */
    @Optional
    @Size(min = 1, max = 16)
    @QrchPath("UltmtCdtr/PstCd")
    @Description("Postal code of the ultimate creditor<br>Maximum 16 characters permitted; is always to be entered without a country code prefix.")
    @Example("2501")
    @Override
    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public void setPostalCode(final int postalCode) {
        setPostalCode(String.valueOf(postalCode));
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>City<br>City of the ultimate creditor</td><td>Maximum 35 characters permitted</td></tr>
     * <tr><td>DE</td><td>Ort<br>Ort der endgültigen Zahlungsempfängers</td><td>Maximal 35 Zeichen zulässig</td></tr>
     * <tr><td>FR</td><td>Lieu<br>Lieu du bénéficiaire final</td><td>35 caractères au maximum admis</td></tr>
     * <tr><td>IT</td><td>Località<br>Località dell’indirizzo del beneficiario finale</td><td>Ammessi massimo 35 caratteri</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++TwnNm
     * </pre>
     */
    @Optional
    @Size(min = 1, max = 35)
    @QrchPath("UltmtCdtr/TwnNm")
    @Description("City of the ultimate creditor<br>Maximum 35 characters permitted")
    @Example("Biel")
    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(final String city) {
        this.city = city;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Address line 1<br>Address line 1 including street and building number or P.O. Box</td><td>Maximum 70 characters permitted</td></tr>
     * <tr><td>DE</td><td>Adresszeile 1<br>Adresszeile 1 mit Strasse und Hausnummer bzw. Postfach</td><td>Maximal 70 Zeichen zulässig</td></tr>
     * <tr><td>FR</td><td>Ligne d'adresse 1<br>Ligne d'adresse 1 avec rue et numéro de maison ou case postale</td><td>70 caractères au maximum admis</td></tr>
     * <tr><td>IT</td><td><br></td><td></td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++StrtNmOrAdrLine1
     * </pre>
     */
    @Optional
    @Size(min = 1, max = 70)
    @QrchPath("UltmtCdtr/StrtNmOrAdrLine1")
    @Description("Address line 1 including street and building number or P.O. Box<br>Maximum 70 characters permitted")
    @Example("Rue du Lac 1268/3/1")
    @Override
    public String getAddressLine1() {
        return addressLine1;
    }

    @Override
    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Address line 2<br>Address line 2 including postal code and town from ultimate creditor’s address<br></td><td>Maximum 70 characters permitted<br>Must be provided for address type "K".</td></tr>
     * <tr><td>DE</td><td>Adresszeile 2<br></td><td>Adresszeile 2 mit Postleitzahl und Ort der endgültigen Zahlungsempfängeradresse</td><td>maximal 70 Zeichen zulässig<br>Muss geliefert werden bei Adress-Typ "K".</td></tr>
     * <tr><td>FR</td><td>Ligne d'adresse 2<br>Ligne d'adresse 2 avec numéro postal d'acheminement et localité de l'adresse du créancier final</td><td>70 caractères au maximum admis<br>Doit être livré pour le type d'adresse "K".</td></tr>
     * <tr><td>IT</td><td><br></td><td></td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++BldgNbOrAdrLine2
     * </pre>
     */
    @Optional
    @Size(min = 1, max = 70)
    @QrchPath("UltmtCdtr/BldgNbOrAdrLine2")
    @Description("Address line 2 including postal code and town from ultimate creditor’s address<br>Maximum 70 characters permitted<br>Must be provided for address type \"K\".")
    @Example("2501 Biel")
    @Override
    public String getAddressLine2() {
        return addressLine2;
    }

    @Override
    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Country<br>Country of the ultimate creditor</td><td>Two-digit country code according to ISO 3166-1</td></tr>
     * <tr><td>DE</td><td>Land<br>Land des endgültigen Zahlungsempfängers</td><td>Zweistelliger Landescode gemäss ISO 3166-1</td></tr>
     * <tr><td>FR</td><td>Pays<br>Pays du bénéficiaire final</td><td>Code de pays à deux positions selon ISO 3166-1</td></tr>
     * <tr><td>IT</td><td>Nazione<br>Nazione dell’indirizzo del beneficiario finale</td><td>Codice nazione a due caratteri secondo ISO 3166-1</td></tr>
     * </table>
     * <p>Status: {@link Dependent}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +UltmtCdtr
     * ++Ctry
     * </pre>
     */
    @Dependent
    @Size(min = 2, max = 2)
    @QrchPath("UltmtCdtr/Ctry")
    @Description("Country of the ultimate creditor<br>Two-digit country code according to ISO 3166-1")
    @Example("CH")
    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public void setCountry(final String country) {
        this.country = country;
    }

    @Override
    public boolean isEmpty() {
        return AddressUtils.isEmpty(this);
    }

    @Override
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return "UltimateCreditor{" +
                "addressType='" + addressType + '\'' +
                ", name='" + name + '\'' +
                ", streetName='" + streetName + '\'' +
                ", houseNumber='" + houseNumber + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UltimateCreditor that = (UltimateCreditor) o;
        return Objects.equals(addressType, that.addressType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(streetName, that.streetName) &&
                Objects.equals(houseNumber, that.houseNumber) &&
                Objects.equals(postalCode, that.postalCode) &&
                Objects.equals(city, that.city) &&
                Objects.equals(addressLine1, that.addressLine1) &&
                Objects.equals(addressLine2, that.addressLine2) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressType, name, streetName, houseNumber, postalCode, city, addressLine1, addressLine2, country);
    }
}
