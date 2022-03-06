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
package ch.codeblock.qrinvoice.model.billinformation.swicos1v12;

public enum Tag {
    /**
     * Bereich: Belegnummer<br>
     * Was: Rechnungsnummer<br>
     * Wertebeispiel: /10/10201409<br>
     * Anmerkung: freier Text<br>
     */
    INVOICE_REFERENCE(10),
    /**
     * Bereich: Belegdatum<br>
     * Was: Belegdatum<br>
     * Wertebeispiel: /11/190512<br>
     * Anmerkung: 12.05.2019<br>
     *
     * <p>Das Belegdatum entspricht dem Rechnungsdatum; es dient als Referenzdatum für die Konditionen.</p>
     * <p>Zusammen mit dem Feld /40/0:n kann ein Fälligkeitsdatum der Rechnung berechnet werden (Zahlbar bis n Tage nach Belegdatum)</p>
     */
    INVOICE_DATE(11),
    /**
     * Bereich: Kundenreferenz<br>
     * Was: Kundenreferenz<br>
     * Wertebeispiel: /20/140.000-53<br>
     * Anmerkung: freier Text<br>
     *
     * <p>Die Kundenreferenz ist eine vom Kunden mitgeteilte Referenz und dient diesem bei der Rechnungseingangsverarbeitung für die Zuordnung der
     * Rechnung.</p>
     */
    CUSTOMER_REFERENCE(20),
    /**
     * Bereich: MWST Nummer<br>
     * Was: UID Nummer<br>
     * Wertebeispiel: /30/106017086<br>
     * Anmerkung: UID CHE-106.017.086 ohne CHE-Präfix, ohne Trennzeichen und ohne MWST/TVA/IVA/VAT-Suffix<br>
     *
     * <p>Die MWST-Nummer entspricht der numerischen UID des Leistungsbringers (ohne CHE-Präfix, Trennzeichen und Zusatz MWST).</p>
     * <p>Die MWST-Nummer kann vom Rechnungsempfänger benutzt werden, um den Rechnungssteller eindeutig zu identifizieren. Jeder Rechnungssteller, der über eine UID verfügt, soll diese hier mitführen, auch wenn die anderen MWST-Felder weggelassen werden.</p>
     * <p>Bei einer Rechnung mit mehreren MWST-Nummern muss die erste angegeben werden.</p>
     */
    VAT_UID_NUMBER(30),
    /**
     * Bereich: MWST Datum<br>
     * Was: Datum oder Anfang- und Enddatum der Leistung<br>
     * Wertebeispiel: <br>
     * - /31/180508 <br>
     * - /31/181001190131<br>
     * Anmerkung:<br>
     * - 08.05.2018<br>
     * - 01.10.2018 bis 31.01.2019<br>
     *
     * <p>Das MWST-Datum kann entweder dem Leistungsdatum oder dem Anfang- und Enddatum der Leistung entsprechen (z.B. bei einem Abonnement).</p>
     * <p>Wenn das Dokument mehrere Leistungen mit unterschiedlichen Leistungsdaten vorweist, muss das Feld /31/ weggelassen werden (manuelle Erfassung).</p>
     */
    VAT_DATE(31),
    /**
     * Bereich: MWST Details<br>
     * Was: Satz der Rechnung oder Liste der Sätze mit entsprechenden Nettobeträgen<br>
     * Wertebeispiel: <br>
     * - /32/7.7<br>
     * - /32/8:1000;2.5:51.8;7.7:250<br>
     * Anmerkung:<br>
     * - 7.7% auf die gesamte Rechnung<br>
     * - 8.0% auf 1000.00, 2.5% auf 51.80 und 7.7% auf 250.00<br>
     *     
     * <p>Die MWST-Details beziehen sich auf den Betrag der Rechnung, ohne Skonto.</p>
     * <p>MWST-Details enthalten entweder<br>
     * – einen einzigen Prozentsatz, der auf den gesamten Betrag der Rechnung anzuwenden ist, oder<br>
     * – eine Liste der MWST-Beträge, definiert durch einen Prozentsatz und einem Nettobetrag; der Doppelpunkt «:» dient als Separator.
     * </p>
     * <p>Der Nettobetrag entspricht dem Nettopreis (exklusiv MWST), auf den die MWST gerechnet wird.</p>
     * <p>Falls eine Liste angegeben wird, müssen die Summe der Nettobeträge und deren berechnete MWST dem Betrag des QR-Codes entsprechen</p>
     */
    VAT_DETAILS(32),
    /**
     * Bereich: MWST Einfuhrsteuer<br>
     * Was: Reiner MWST Betrag oder Liste der reinen MWST Beträge und entsprechenden Sätze bei Einfuhr<br>
     * Wertebeispiel:<br>
     * - /33/7.7:16.15<br>
     * - /33/7.7:48.37;2.5:12.4<br>
     * Anmerkung:<br>
     * - 16.15 reine MWST (7.7% Satz) bei einem Warenimport<br>
     * - 48.37 reine MWST (7.7% Satz) und 12.40 reine MWST (2.5% Satz) bei einem Warenimport mit mehreren Sätzen<br>
     *     
     * <p>Bei Warenimport kann die Einfuhrsteuer in diesem Feld angegeben werden. Es handelt sich dabei um einen reinen MWST-Betrag.</p>
     * <p>Der Satz dient der korrekten Verbuchung der MWST in der Finanzbuchhaltung.</p>
     * <p>Dies vereinfacht dem Rechnungsempfänger beim Import die Verbuchung der MWST.</p>
     */
    IMPORT_TAX(33),
    /**
     * Bereich: Konditionen<br>
     * Was: Konditionen oder Liste der Konditionen<br>
     * Wertebeispiel: <br>
     * - /40/0:30<br>
     * - /40/2:10;0:60<br>
     * - /40/3:15;0.5:45;0:90<br>
     * Anmerkung: <br>
     * - 0% Skonto auf 30 Tage (zahlbar bis 30 Tage nach Belegdatum)<br>
     * - 2% Skonto auf 10 Tage, 0% auf 60 Tage<br>
     * - 3% Skonto auf 15 Tage, 0.5% auf 45 Tage, 0% auf 90 Tage<br>
     *     
     * <p>Die Konditionen können ein Skonto oder eine Liste von Skonti auflisten.</p>
     * <p>Das Belegdatum /11/ dient als Referenzdatum.</p>
     * <p>Jeder Skonto ist durch einen Prozentsatz und eine Frist (Tage) definiert; der Doppelpunkt «:» dient als Separator.</p>
     * <p>Die Angabe mit einem Prozentsatz gleich Null definiert die defaultmässige Zahlungsfrist der Rechnung (z.B. «0:30» für 30 Tage Netto).</p>
     * 
     * <p>Achtung: wenn dieser Tag verwendet wird, sollte wenigstens die defaultmässige Zahlungsfrist der Rechnung angeben werden. Ohne diese Angabe,
     * kann die Zahlsoftware kein Datum für die Zahlung vorschlagen.</p>
     */
    PAYMENT_CONDITIONS(40),

    /**
     * Catch all tag
     */
    UNKNOWN(-1);

    private int tagNr;

    Tag(final int tagNr) {
        this.tagNr = tagNr;
    }

    public int getTagNr() {
        return tagNr;
    }

    public static Tag of(int tagNr) {
        for (final Tag tag : values()) {
            if (tag.getTagNr() == tagNr) {
                return tag;
            }
        }

        return UNKNOWN;
    }
}
