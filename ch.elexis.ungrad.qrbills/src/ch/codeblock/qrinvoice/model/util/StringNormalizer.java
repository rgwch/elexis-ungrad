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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringNormalizer {
    private static final Logger logger = LoggerFactory.getLogger(StringNormalizer.class);

    private boolean trim;
    private boolean replaceLineBreaks;
    private boolean replaceTabs;
    private boolean replaceCharactersSameLength;
    private boolean replaceCharactersWithMultipleChars;

    private StringNormalizer() {
    }

    public static StringNormalizer create() {
        return new StringNormalizer();
    }

    public StringNormalizer enableTrim() {
        return enableTrim(true);
    }

    public StringNormalizer enableTrim(boolean enabled) {
        this.trim = enabled;
        return this;
    }

    public StringNormalizer enableReplaceLineBreaks() {
        return enableReplaceLineBreaks(true);
    }

    public StringNormalizer enableReplaceLineBreaks(boolean enabled) {
        this.replaceLineBreaks = enabled;
        return this;
    }

    public StringNormalizer enableReplaceTabs() {
        return enableReplaceTabs(true);
    }

    public StringNormalizer enableReplaceTabs(boolean enabled) {
        this.replaceTabs = enabled;
        return this;
    }

    /**
     * Enables both {@link #enableReplaceCharactersSameLength()} and {@link #enableReplaceCharactersWithMultipleChars()}
     *
     * @return self
     */
    public StringNormalizer enableReplaceCharacters() {
        enableReplaceCharactersSameLength();
        enableReplaceCharactersWithMultipleChars();
        return this;
    }

    /**
     * Sets both {@link #enableReplaceCharactersSameLength(boolean)} and {@link #enableReplaceCharactersWithMultipleChars(boolean)}
     *
     * @return self
     */
    public StringNormalizer enableReplaceCharacters(boolean enabled) {
        enableReplaceCharactersSameLength(enabled);
        enableReplaceCharactersSameLength(enabled);
        return this;
    }

    public StringNormalizer enableReplaceCharactersSameLength() {
        return enableReplaceCharactersSameLength(true);
    }

    public StringNormalizer enableReplaceCharactersSameLength(boolean enabled) {
        this.replaceCharactersSameLength = enabled;
        return this;
    }

    public StringNormalizer enableReplaceCharactersWithMultipleChars() {
        return enableReplaceCharactersWithMultipleChars(true);
    }

    public StringNormalizer enableReplaceCharactersWithMultipleChars(boolean enabled) {
        this.replaceCharactersWithMultipleChars = enabled;
        return this;
    }

    public String normalize(String input) {
        if (input == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(input);
        for (int i = 0; i < sb.length(); i++) {
            final boolean hasMoreChars = i < sb.length() - 1;
            final char chr = sb.charAt(i);

            if (replaceLineBreaks) {
                // first replace \r\n by \r in order to get rid of duplicate spaces
                if (chr == '\r' && hasMoreChars && sb.charAt(i + 1) == '\n') {
                    sb.deleteCharAt(i + 1);
                }

                if (chr == '\r' || chr == '\n') {
                    sb.setCharAt(i, ' ');
                }
            }

            if (replaceTabs && chr == '\t') {
                sb.setCharAt(i, ' ');
            }

            if (replaceCharactersSameLength) {
                if (chr == '\u00A0') { //  
                    sb.setCharAt(i, ' ');
                }

                if (chr == '\u00AD') { // ­ - soft hyphens can be safely removed
                    sb.deleteCharAt(i--);
                }
                if (chr == '\u00B0') { // °
                    sb.setCharAt(i, '.');
                }

                // replace all single-quotes and "single-quotes-looking" chars with simple single quote
                if (chr == '\u2018' || chr == '\u2019') { // ‘ ’
                    sb.setCharAt(i, '\'');
                }

                // replace all double-quotes and "double-quotes-looking" chars with simple double quote
                if (chr == '\u201C' || chr == '\u201D' || chr == '\u201E' || chr == '\u201F' || // “ ” „ ‟
                        chr == '\u00AB' || chr == '\u00BB' // « »
                ) { // ‘ and ’
                    sb.setCharAt(i, '"');
                }

                // replace all dash variants and the middle dot '·'
                if (chr == '\u2010' || chr == '\u2011' || chr == '\u2012' || chr == '\u2013' || chr == '\u2014' || chr == '\u2015'
                        || chr == '\u00B7') { // ·
                    sb.setCharAt(i, '-');
                }

                if (chr == '\u00C3' || chr == '\u00C5') { // Ã Å
                    sb.setCharAt(i, 'A');
                }
                if (chr == '\u00E3' || chr == '\u00E5') { // ã å
                    sb.setCharAt(i, 'a');
                }

                if (chr == '\u00D5') { // Õ
                    sb.setCharAt(i, 'O');
                }
                if (chr == '\u00F5') { // õ
                    sb.setCharAt(i, 'o');
                }

                if (chr == '\u00DD' || chr == '\u0178') { // Ý Ÿ
                    sb.setCharAt(i, 'Y');
                }
                if (chr == '\u00FF') { // ÿ
                    sb.setCharAt(i, 'y');
                }

                if (chr == '\u00D8') { // Ø
                    sb.setCharAt(i, 'Ö');
                }
                if (chr == '\u00F8') { // ø
                    sb.setCharAt(i, 'ö');
                }
                if (chr == '\u00D7') { // ×
                    sb.setCharAt(i, 'x');
                }
                if (chr == '\u00D0') { // Ð
                    sb.setCharAt(i, 'D');
                }
                if (chr == '\u00F0') { // ð
                    sb.setCharAt(i, 'd');
                }

                if (chr == '\u00B9') { // ¹
                    sb.setCharAt(i, '1');
                }
                if (chr == '\u00B2') { // ²
                    sb.setCharAt(i, '2');
                }
                if (chr == '\u00B3') { // ³
                    sb.setCharAt(i, '3');
                }

                // just remove it
                if (chr == '\u00A8') { // ¨
                    sb.deleteCharAt(i--);
                }
            }
            if (replaceCharactersWithMultipleChars) {
                if (chr == '\u00C6') { // Æ
                    sb.setCharAt(i, 'A');
                    sb.insert(++i, 'e');
                }
                if (chr == '\u00E6') { // æ
                    sb.setCharAt(i, 'a');
                    sb.insert(++i, 'e');
                }
                if (chr == '\u0152') { // Œ
                    sb.setCharAt(i, 'O');
                    sb.insert(++i, 'e');
                }
                if (chr == '\u0153') { // œ
                    sb.setCharAt(i, 'o');
                    sb.insert(++i, 'e');
                }
                // euro sign is not supported, but might not be that unusual, therefore replace it with "EUR" if present
                if (chr == '\u20AC') { // €
                    sb.setCharAt(i, 'E');
                    sb.insert(++i, 'U');
                    sb.insert(++i, 'R');
                }
                // copyright and trademark signs are not supported, simple replacement is ok
                if (chr == '\u00A9') { // ©
                    sb.setCharAt(i, '(');
                    sb.insert(++i, 'c');
                    sb.insert(++i, ')');
                }
                if (chr == '\u00AE') { // ®
                    sb.setCharAt(i, '(');
                    sb.insert(++i, 'r');
                    sb.insert(++i, ')');
                }
            }
        }

        String result = sb.toString();
        if (trim) {
            result = result.trim();
        }

        if (logger.isDebugEnabled() && !input.equals(result)) {
            logger.debug("String normalized - input={} result={}", input, sb);
        }

        return result;
    }

    public StringNormalizer enableAll() {
        return enableTrim()
                .enableReplaceLineBreaks()
                .enableReplaceTabs()
                .enableReplaceCharactersSameLength()
                .enableReplaceCharactersWithMultipleChars();
    }
}
