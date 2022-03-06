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
package ch.codeblock.qrinvoice.model.util;

public class AlternativeSchemesUtils {
    private AlternativeSchemesUtils() {
    }

    public static AlternativeSchemePair parseForOutput(String alternativeScheme) {
        if (alternativeScheme == null) {
            return new AlternativeSchemePair(null, null);
        }
        
        final int posName = alternativeScheme.indexOf(": ");
        if (posName > -1) {
            final String name = alternativeScheme.substring(0, posName + 1);
            final String value = alternativeScheme.substring(posName + 1, alternativeScheme.length());

            return new AlternativeSchemePair(name, value);
        } else {
            return new AlternativeSchemePair(null, alternativeScheme);
        }
    }

    public static class AlternativeSchemePair {
        private final String name;
        private final String value;

        public AlternativeSchemePair(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public boolean hasName() {
            return name != null;
        }

        public boolean hasValue() {
            return value != null;
        }

        public boolean isEmpty() {
            return name == null && value == null;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
