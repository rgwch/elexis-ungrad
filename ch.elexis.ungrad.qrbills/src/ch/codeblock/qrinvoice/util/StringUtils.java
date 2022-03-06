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
package ch.codeblock.qrinvoice.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        return cs == null || cs.toString().trim().length() == 0;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static String emptyStringAsNull(final String str) {
        if (str != null && str.isEmpty()) {
            return null;
        } else {
            return str;
        }
    }

    public static boolean startsWith(String str, String prefix) {
        if (prefix == null) {
            throw new NullPointerException("prefix must not be null");
        }
        if (str == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static int length(final CharSequence... cs) {
        return Arrays.stream(cs).mapToInt(StringUtils::length).sum();
    }

    public static String join(CharSequence delimiter, CharSequence... cs) {
        if (cs == null) {
            throw new IllegalArgumentException("Object varargs must not be null");
        }
        if (delimiter == null) {
            throw new IllegalArgumentException("Separator must not be null");
        }

        return Arrays.stream(cs).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");

    public static boolean isNumeric(String s) {
        return s != null && NUMERIC_PATTERN.matcher(s).matches();
    }

    private static final Pattern UNSIGNED_INTEGER_PATTERN = Pattern.compile("\\d+");

    public static boolean isUnsignedIntegerNumber(String s) {
        return s != null && UNSIGNED_INTEGER_PATTERN.matcher(s).matches();
    }

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    /**
     * Remove whitespaces from string
     *
     * @param input Input string
     * @return Cleaned input string with whitespaces removed. Returns empty string if the input was null.
     */
    public static String removeWhitespaces(final String input) {
        if (input == null) {
            return "";
        }

        return WHITESPACE_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Check if a string contains a whitespace
     *
     * @param input Input string
     * @return true, if any whitespace is found in the given input string. False otherwise
     */
    public static boolean containsWhitespace(final String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        return WHITESPACE_PATTERN.matcher(input).find();
    }

    public static boolean isTrimmable(final String value) {
        return isNotEmpty(value) && (value.startsWith(" ") || value.endsWith(" "));
    }

    public static String leftPad(String value, final int size, final char padChar) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= size) {
            return value;
        }
        final StringBuilder sb = new StringBuilder();
        while (sb.length() < size - value.length()) {
            sb.append(padChar);
        }
        sb.append(value);

        return sb.toString();
    }

    public static String escapeControlCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String trimToEmpty(final String str) {
        return str == null ? "" : str.trim();
    }

    public static String trimToNull(String str) {
        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        } else {
            if (start < 0) {
                start += str.length();
            }

            if (start < 0) {
                start = 0;
            }

            return start > str.length() ? "" : str.substring(start);
        }
    }

    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        } else {
            if (end < 0) {
                end += str.length();
            }

            if (start < 0) {
                start += str.length();
            }

            if (end > str.length()) {
                end = str.length();
            }

            if (start > end) {
                return "";
            } else {
                if (start < 0) {
                    start = 0;
                }

                if (end < 0) {
                    end = 0;
                }

                return str.substring(start, end);
            }
        }
    }
}
