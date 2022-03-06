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
package ch.codeblock.qrinvoice.model.validation;

import java.io.Serializable;
import java.util.Objects;

import static ch.codeblock.qrinvoice.util.StringUtils.escapeControlCharacters;

public class InvalidCharacterSequence implements Serializable {
    private static final long serialVersionUID = 2813844649294431461L;

    private static final int CONTEXT_LENGTH = 4;

    private final String input;
    private final String invalidCharSequence;
    private final int index;

    private final String errorSummary;

    protected InvalidCharacterSequence(final String input, final String invalidCharSequence, final int index) {
        this.input = input;
        this.invalidCharSequence = invalidCharSequence;
        this.index = index;

        final int start = index;
        final int end = index + invalidCharSequence.length();

        final String charsBeforeSequence = escapeControlCharacters(input.substring(Math.max(start - CONTEXT_LENGTH, 0), start));
        final String charsAfterSequence = escapeControlCharacters(input.substring(end, Math.min(end + CONTEXT_LENGTH, input.length())));
        final String invalidCharSequenceForMessage = escapeControlCharacters(this.invalidCharSequence);
        final String context = String.format("%s<%s>%s", charsBeforeSequence, invalidCharSequenceForMessage, charsAfterSequence);
        errorSummary = String.format("Invalid character(s) '%s' at index %s with a length of %s (context: '%s')", invalidCharSequenceForMessage, start, this.invalidCharSequence.length(), context);
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public String getInvalidCharSequence() {
        return invalidCharSequence;
    }

    public int getInvalidCharSequenceStartIndex() {
        return index;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final InvalidCharacterSequence that = (InvalidCharacterSequence) o;
        return index == that.index &&
                Objects.equals(input, that.input) &&
                Objects.equals(invalidCharSequence, that.invalidCharSequence) &&
                Objects.equals(errorSummary, that.errorSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, invalidCharSequence, index, errorSummary);
    }
}
