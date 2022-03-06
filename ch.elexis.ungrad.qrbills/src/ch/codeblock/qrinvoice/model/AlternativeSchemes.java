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

import ch.codeblock.qrinvoice.model.annotation.Additional;
import ch.codeblock.qrinvoice.model.annotation.Description;
import ch.codeblock.qrinvoice.model.annotation.Example;
import ch.codeblock.qrinvoice.model.annotation.QrchPath;

import java.util.List;
import java.util.Objects;

/**
 * <p>From the specification v2.0</p>
 * <table border="1" summary="Excerpt from the specification">
 * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
 * <tr><td>EN</td><td>Alternative schemes<br>Parameters and data of other supported schemes</td><td>Optional data group with a variable number of elements</td></tr>
 * <tr><td>DE</td><td>Alternative Verfahren<br>Parameter und Daten weiterer unterstützter Verfahren</td><td>Optionale Datengruppe mit variabler Anzahl von Elementen</td></tr>
 * <tr><td>FR</td><td>Procédures alternatives<br>Paramètres et données d'autres procédures supportées</td><td>Groupe de données optionnel avec un nombre variable d'éléments</td></tr>
 * <tr><td>IT</td><td>Processi alternativi<br>Parametri e dati di altri processi supportati</td><td>Gruppo di dati opzionale con numero variabile di elementi</td></tr>
 * </table>
 * <p>Data Structure Element</p>
 * <pre>
 * QRCH
 * +AltPmtInf
 * </pre>
 */
public class AlternativeSchemes {
    private List<String> alternativeSchemeParameters;

    /**
     * <p>From the specification v2.0</p>
     * <table border="1" summary="Excerpt from the specification">
     * <tr><th>Language</th><th>General Definition</th><th>Field Definition</th></tr>
     * <tr><td>EN</td><td>Alternative scheme parameters<br>Parameter character chain of the alternative scheme according to the syntax definition in the "Alternative scheme" section</td><td>Can be currently delivered a maximum of two times.<br>Maximum 100 characters</td></tr>
     * <tr><td>DE</td><td>Parameter alternatives Verfahren<br>Parameter-Zeichenkette des alternativen Verfahrens gemäss Syntaxdefinition in Kapitel «Alternative Verfahren»</td><td>Kann aktuell maximal zweimal geliefert werden.<br>Maximal 100 Zeichen</td></tr>
     * <tr><td>FR</td><td>Paramètres de procédure alternative<br>Chaîne de caractères de paramètres de la procédure alternative selon définition de syntaxe dans le chapitre «Procédures alternatives»</td><td>Peuvent actuellement être livrés deux fois au maximum. 100 caractères au maximum </td></tr>
     * <tr><td>IT</td><td>Parametri processo alternativo<br>Serie di caratteri dei parametri del processo alternativo in base alla definizione sintattica del Capitolo «Processo alternativo»</td><td>Attualmente può essere inviato al massimo due volte. Massimo 100 caratteri</td></tr>
     * </table>
     * <p>Status: {@link Additional}</p>
     * <p>Data Structure Element</p>
     * <pre>
     * QRCH
     * +AltPmtInf
     * ++AltPmt
     * </pre>
     */
    @Additional
    @QrchPath("AltPmtInf/AltPmt")
    @Description("Alternative scheme parameters<br>Parameter character chain of the alternative scheme according to the syntax definition in the “Alternative scheme” section<br>Can be currently delivered a maximum of two times.<br>Maximum 100 characters")
    @Example("Name AV1: UV;UltraPay005;12345")
    public List<String> getAlternativeSchemeParameters() {
        return alternativeSchemeParameters;
    }

    public void setAlternativeSchemeParameters(final List<String> alternativeSchemeParameters) {
        this.alternativeSchemeParameters = alternativeSchemeParameters;
    }

    @Override
    public String toString() {
        return "AlternativeSchemes{" +
                "alternativeSchemeParameters='" + alternativeSchemeParameters + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AlternativeSchemes that = (AlternativeSchemes) o;
        return Objects.equals(alternativeSchemeParameters, that.alternativeSchemeParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alternativeSchemeParameters);
    }
}
