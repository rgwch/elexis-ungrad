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

import java.math.BigInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ch.codeblock.qrinvoice.util.StringUtils.removeWhitespaces;

/**
 * Util for Creditor Reference according to ISO 11649
 */
public final class CreditorReferenceUtils {

    private static final Pattern PATTERN = Pattern.compile("^[0-9a-zA-Z]*$");

    private CreditorReferenceUtils() {
    }

    private static final int CHARS_PER_BLOCK = 4;

    /**
     * In the beginning there are two letters 'RF'
     */
    private static final String PREFIX = "RF";
    /**
     * at least four chars expected (RF + 2 Digits Checksum)
     */
    private static final int MIN_LENGTH = 4;
    /**
     * The Creditor Reference is 25 characters long and alphanumeric
     */
    private static final int MAX_LENGTH = 25;
    private static final BigInteger MOD_97 = BigInteger.valueOf(97);
    private static final BigInteger MOD_97_REMAINDER = BigInteger.ONE;

    /**
     * <p>Validates if the given input is a valid Creditor Reference</p>
     *
     * @param creditorReferenceInput The Creditor Reference String
     * @return true, if the given String is a valid creditor reference
     * @see <a href="https://en.wikipedia.org/wiki/Creditor_Reference">Creditor_Reference</a>
     */
    public static boolean isValidCreditorReference(final String creditorReferenceInput) {
        if (creditorReferenceInput == null) {
            return false;
        }

        // first, remove all whitespaces
        // --> RF45 1234 5123 45 -> RF451234512345
        final String creditorReference = normalizeCreditorReference(creditorReferenceInput);

        // 1. perform basic length an prefix checks
        final int length = creditorReference.length();
        if (MIN_LENGTH > length || length > MAX_LENGTH) {
            return false;
        }

        if (!creditorReference.startsWith(PREFIX)) {
            return false;
        }
        
        if(!validCharacters(creditorReference)) {
            return false;
        }

        // 2. Move the four initial characters to the end of the string
        // --> RF451234512345 -> 1234512345RF45
        final String creditorReferenceRearranged = creditorReference.substring(4) + creditorReference.substring(0, 4);

        // 3. Replace each letter in the string with two digits, thereby expanding the string, where A = 10, B = 11, ..., Z = 35.
        // --> 1234512345RF45 -> 1234512345271545
        final String numericCreditorReference = convertToNumericRepresentation(creditorReferenceRearranged);

        // 4. Interpret the string as a decimal integer and compute the remainder of that number on division by 97
        final int mod = getMod97Remainder(numericCreditorReference);

        // true and valid, if remainder equals 1
        return mod == MOD_97_REMAINDER.intValue();
    }

    static int getMod97Remainder(final String numericCreditorReference) {
        final BigInteger creditorReferenceNumber = new BigInteger(numericCreditorReference);
        return creditorReferenceNumber.mod(MOD_97).intValue();
    }

    /**
     * @param str a string containing alphanumeric characters, e.g. 1234512345RF45
     * @return the string with numeric characters only, alphanumeric characters were mapped to their ascii code representation e.g. 1234512345271545
     */
    static String convertToNumericRepresentation(final String str) {
        if (!validCharacters(str)) {
            throw new IllegalArgumentException("Encountered illegal characters");
        }
        return str.chars()
                .map(Character::getNumericValue)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    private static boolean validCharacters(final String str) {
        return PATTERN.matcher(str).matches();
    }

    /**
     * <p>Validates if the given input is a valid Creditor Reference</p>
     *
     * @param creditorReferenceInput The Creditor Reference String
     * @return true, if the given String is a valid creditor reference
     * @see <a href="https://en.wikipedia.org/wiki/Creditor_Reference">Creditor_Reference</a>
     * @deprecated use {@link #isValidCreditorReference(String)} instead
     */
    @Deprecated
    public static boolean isValid(final String creditorReferenceInput) {
        return isValidCreditorReference(creditorReferenceInput);
    }

    /**
     * Formats the Creditor Reference number for output
     * @param creditorReferenceInput e.g. RF451234512345
     * @return formatted for print or display output e.g. RF45 1234 5123 45
     */
    public static String formatCreditorReference(final String creditorReferenceInput) {
        if (creditorReferenceInput == null) {
            return "";
        }

        final String creditorReference = normalizeCreditorReference(creditorReferenceInput);
        final StringBuilder creditorReferenceBuffer = new StringBuilder(creditorReference);

        final int numberOfBlocks = (int) (Math.ceil((double) creditorReference.length() / (double) CHARS_PER_BLOCK));
        for (int blockNr = 1; blockNr < numberOfBlocks; blockNr++) {
            creditorReferenceBuffer.insert((blockNr * CHARS_PER_BLOCK) + blockNr - 1, ' ');
        }

        return creditorReferenceBuffer.toString();
    }

    /**
     * Normalizes a Creditor Reference - does the exact opposite of {@link #formatCreditorReference(String)}
     *
     * @param creditorReferenceInput The Creditor Reference to normalize, may or may not contain whitespaces (e.g. RF45 1234 5123 45)
     * @return The normalized creditor reference (e.g. RF451234512345), which means the creditor reference without any whitespaces or an empty string, if null was passed
     */
    public static String normalizeCreditorReference(final String creditorReferenceInput) {
        if (creditorReferenceInput == null) {
            return "";
        }
        return removeWhitespaces(creditorReferenceInput).toUpperCase();
    }

    /**
     * Creates a normalized Creditor Reference
     *
     * @param creditorReferenceInput A string that may already be a valid Creditor Reference number or just any number (like invoice nr) that needs to be converted
     * @return The normalized Creditor Reference (e.g. RF451234512345)
     */
    public static String createCreditorReference(final String creditorReferenceInput) {
        if (StringUtils.isBlank(creditorReferenceInput)) {
            throw new IllegalArgumentException("Input must not be empty");
        }

        final String normalizedCreditorReference = normalizeCreditorReference(creditorReferenceInput);
        if (isValidCreditorReference(normalizedCreditorReference)) {
            return normalizedCreditorReference;
        } else if (normalizedCreditorReference.length() == MAX_LENGTH) {
            throw new IllegalArgumentException("Input reference length equals Creditor reference number max length of '" + MAX_LENGTH + "' but is not a valid Creditor reference");
        }

        if (normalizedCreditorReference.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid Creditor reference number - max length of '" + MAX_LENGTH + "' exceeded");
        } else {
            // we build the creditor reference in the format used to validate the checksum (e.g. <referenc><RF><checkdigits>)
            // e.g. input is 42 -> 42RF00 whereas checkdigits are left to 00 which makes it easy to determine the correct checkdigits
            final String checkDigits = calculateCheckDigitsForReferenceNumber(normalizedCreditorReference);
            final String newCreditorReferenceNumber = PREFIX + checkDigits + normalizedCreditorReference;

            if (isValidCreditorReference(newCreditorReferenceNumber)) {
                return newCreditorReferenceNumber;
            } else {
                throw new IllegalArgumentException("Invalid Creditor reference number");
            }
        }

    }

    public static String calculateCheckDigitsForReferenceNumber(final String input) {
        return calculateCheckDigits(convertToNumericRepresentation(input + PREFIX + "00"));
    }

    public static String calculateCheckDigits(String input) {
        if (StringUtils.isBlank(input) || input.length() < 3) {
            throw new IllegalArgumentException("Input must not be empty and be at least 3 digits long");
        }
        if (!input.substring(input.length() - 2).equals("00")) {
            throw new IllegalArgumentException("Input must end with two zeros '00'");
        }

        final int mod97Remainder = getMod97Remainder(input);
        // e.g. 97 - 53 + 1 (whereas 53 is determined by input)
        final int checksum = MOD_97.intValue() - mod97Remainder + MOD_97_REMAINDER.intValue();
        if (checksum >= 10) {
            return String.valueOf(checksum);
        } else {
            return "0" + checksum;
        }
    }

}
