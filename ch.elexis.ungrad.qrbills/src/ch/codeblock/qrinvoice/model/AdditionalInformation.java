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
import ch.codeblock.qrinvoice.model.billinformation.BillInformation;

import java.util.Objects;

/**
 * <p>From the specification v2.0</p>
 * <table border="1" summary="Excerpt from the specification">
 * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
 * <tr><td>EN</td><td>Additional information<br>Additional information can be used for the scheme with message and for the scheme with structured reference.</td><td>Mandatory data group</td></tr>
 * <tr><td>DE</td><td>Zusätzliche Informationen<br>Zusätzliche Informationen können beim Verfahren mit Mitteilung und beim Verfahren mit strukturierter Referenz verwendet werden.</td><td>Obligatorische Datengruppe</td></tr>
 * <tr><td>FR</td><td>Informations supplémentaires<br>Des informations supplémentaires peuvent être utilisées pour la procédure avec communication et pour la procédure avec référence structurée.</td><td>Groupe de données obligatoire</td></tr>
 * <tr><td>IT</td><td></td><td>Gruppo di dati obbligatorio</td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +RmtInf
 * ++AddInf
 * </pre>
 */
public class AdditionalInformation {
    private String unstructuredMessage;
    private String trailer;
    private String billInformation;
    private BillInformation billInformationObject;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Unstructured message<br>Unstructured information can be used to indicate the payment purpose or for additional textual information about payments with a structured reference.</td><td>Maximum 140 characters</td></tr>
     * <tr><td>DE</td><td>Unstrukturierte Mitteilung<br>Unstrukturierte Informationen können zur Angabe eines Zahlungszwecks oder für ergänzende textuelle Informationen zu Zahlungen mit strukturierter Referenz verwendet werden.</td><td>Maximal 140 Zeichen</td></tr>
     * <tr><td>FR</td><td>Communication non structurée<br>Les informations instructurées peuvent être utilisées pour l'indication d'un motif de paiement ou pour des informations textuelles complémentaires au sujet de paiements avec référence structurée.</td><td>140 caractères au maximum admis</td></tr>
     * <tr><td>IT</td><td>Messaggio non strutturato<br></td><td>Massimo 140 caratteri</td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +RmtInf
     * ++AddInf
     * +++Ustrd
     * </pre>
     */
    @Optional
    @Size(min = 0, max = 140)
    @QrchPath("RmtInf/AddInf/Ustrd")
    @Description("Unstructured message<br>Unstructured information can be used to indicate the payment purpose or for additional textual information about payments with a structured reference.<br>Maximum 140 characters")
    @Example("Bill No. 3139 for garden work and disposal of cuttings")
    public String getUnstructuredMessage() {
        return unstructuredMessage;
    }

    public void setUnstructuredMessage(final String unstructuredMessage) {
        this.unstructuredMessage = unstructuredMessage;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Trailer<br>Unambiguous indicator for the end of payment data. Fixed value "EPD" (End Payment Data).</td><td>Fixed length: three-digit, alphanumeric</td></tr>
     * <tr><td>DE</td><td>Trailer<br>Eindeutiges Kennzeichen für Ende der Zahlungsdaten. Fixer Wert "EPD" (End Payment Data).</td><td>Feste Länge: 3-stellig, alphanumerisch</td></tr>
     * <tr><td>FR</td><td>Trailer<br>Identifiant univoque pour la fin du code QR. Valeur fixe "EPD" (End Payment Data).</td><td>Longueur fixe: 3 positions alphanumériques</td></tr>
     * <tr><td>IT</td><td></td><td></td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +RmtInf
     * ++AddInf
     * +++Trailer
     * </pre>
     */
    @Optional
    @Size(min = 3, max = 3)
    @QrchPath("RmtInf/AddInf/Trailer")
    @Description("Unambiguous indicator for the end of payment data. Fixed value \"EPD\" (End Payment Data).")
    @Example("EPD")
    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(final String trailer) {
        this.trailer = trailer;
    }

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Bill information<br>Bill information contain coded information for automated booking of the payment. The data is not forwarded with the payment.</td><td>Maximum 140 characters permitted<br>Use of the information is not part of the standardization.</td></tr>
     * <tr><td>DE</td><td>Rechnungsinformationen<br>Rechnungsinformationen enthalten codierte Informationen für die automatisierte Verbuchung der Zahlung. Die Daten werden nicht mit der Zahlung weitergeleitet.</td><td>Maximal 140 Zeichen zulässig</td></tr>
     * <tr><td>FR</td><td>Informations de facture<br>Les informations structurelles de l'émetteur de factures contiennent des informations codées pour la comptabilisation automatisée du paiement. Les données ne sont pas transmises avec le paiement.</td><td>140 caractères au maximum.</td></tr>
     * <tr><td>IT</td><td></td><td></td></tr>
     * </table>
     * <p>Status: {@link Optional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +RmtInf
     * ++AddInf
     * +++StrdBkgInf
     * </pre>
     */
    @Optional
    @Size(min = 0, max = 140)
    @QrchPath("RmtInf/AddInf/StrdBkgInf")
    @Description("Bill information contain coded information for automated booking of the payment. The data is not forwarded with the payment.")
    @Example("//S1/10/10201409/11/190512/20/1400.000-53/30/106017086/31/180508/32/7.7/40/2:10;0:30")
    public String getBillInformation() {
        return billInformation;
    }

    public void setBillInformation(final String billInformation) {
        this.billInformation = billInformation;
    }

    @Optional(hidden = true)
    @QrchPath("RmtInf/AddInf/StrdBkgInf")
    @Description("Bill information as object - same information as in billInformation. See type SwicoS1v12 for example value")
    public BillInformation getBillInformationObject() {
        return billInformationObject;
    }

    public void setBillInformationObject(final BillInformation billInformationObject) {
        this.billInformationObject = billInformationObject;
    }

    @Override
    public String toString() {
        return "AdditionalInformation{" +
                "unstructuredMessage='" + unstructuredMessage + '\'' +
                ", trailer='" + trailer + '\'' +
                ", billInformation='" + billInformation + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AdditionalInformation that = (AdditionalInformation) o;
        return Objects.equals(unstructuredMessage, that.unstructuredMessage) &&
                Objects.equals(trailer, that.trailer) &&
                Objects.equals(billInformation, that.billInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unstructuredMessage, trailer, billInformation);
    }
}
