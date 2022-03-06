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
package ch.codeblock.qrinvoice.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Mandatory field with optional content</p>
 * <table border="1" summary="Excerpt from the specification">
 * <tr><th>Language</th><th>General Definition</th></tr>
 * <tr><td>EN</td><td>Field must mandatorily be delivered, but not necessarily filled (can be empty).</td></tr>
 * <tr><td>DE</td><td>Feld muss zwingend geliefert, aber nicht zwingend befüllt werden (kann leer sein).</td></tr>
 * <tr><td>FR</td><td>Le champ doit être livré impérativement, mais il ne doit pas être rempli impérativement (il peut être vide).</td></tr>
 * <tr><td>IT</td><td>Campo da inviare, ma non da compilare obbligatoriamente (può essere vuoto).</td></tr>
 * </table>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {
    boolean hidden() default false;
}
