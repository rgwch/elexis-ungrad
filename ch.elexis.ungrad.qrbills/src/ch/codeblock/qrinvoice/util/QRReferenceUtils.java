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

import static ch.codeblock.qrinvoice.util.StringUtils.removeWhitespaces;

/**
 * Util for QR-Reference (former ISR reference number)
 */
public final class QRReferenceUtils {
    private QRReferenceUtils() {
    }

    private static final int CHARS_FIRST_BLOCK = 2;
    private static final int CHARS_PER_BLOCK = 5;

    /**
     * The QR Reference must have a length of 27 characters
     */
    public static final int LENGTH = 27;

    /**
     * <p>Validates if the given input is a valid QR-Reference (former ISR reference number)</p>
     * <p>QR reference: 27 characters, numeric, check sum calculation according to Modulo 10 recursive (27th position of the reference)</p>
     *
     * @param qrReferenceInput The QR Reference String
     * @return true, if the given String is a valid QR Reference Number
     * @see <a href="https://www.moneytoday.ch/lexikon/strukturierte-referenz/">QR Reference</a>
     */
    public static boolean isValidQrReference(final String qrReferenceInput) {
        if (qrReferenceInput == null) {
            return false;
        }

        // first, remove all whitespaces
        // --> 11 00012 34560 00000 00008 13457 -> 110001234560000000000813457
        final String qrReference = normalizeQrReference(qrReferenceInput);

        // 1. perform basic length an prefix checks
        if (LENGTH != qrReference.length()) {
            return false;
        }

        // 2. split ESR number to verify checksum digit
        // 110001234560000000000813457 -> 11000123456000000000081345
        final String qrReferenceWithoutChecksumDigit = qrReference.substring(0, 26);
        // 110001234560000000000813457 -> 7
        final int checksumDigit = Character.getNumericValue(qrReference.charAt(26));

        // 3. calculate checksum digit an verify it
        try {
            return modulo10Recursive(qrReferenceWithoutChecksumDigit) == checksumDigit;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * @param qrReferenceInput The QR Reference String
     * @return true, if the given String is a valid QR Reference Number
     * @deprecated use {@link #isValidQrReference(String)} instead
     */
    @Deprecated
    public static boolean isValid(String qrReferenceInput) {
        return isValidQrReference(qrReferenceInput);
    }

    /**
     * Formats the QR Reference number for output
     * @param qrReferenceInput e.g. "110001234560000000000813457" or "11 00012 34560 00000 00008 13457"
     * @return formatted for print or display output '11 00012 34560 00000 00008 13457'
     */
    public static String formatQrReference(final String qrReferenceInput) {
        if (qrReferenceInput == null) {
            return "";
        }

        final String qrReference = normalizeQrReference(qrReferenceInput);
        final StringBuilder qrReferenceBuffer = new StringBuilder(qrReference);

        final int numberOfBlocks = (int) (Math.ceil((double) (qrReference.length() - CHARS_FIRST_BLOCK) / (double) CHARS_PER_BLOCK) + 1.0);
        for (int blockNr = 1; blockNr < numberOfBlocks; blockNr++) {
            if (blockNr == 1) {
                qrReferenceBuffer.insert(CHARS_FIRST_BLOCK, ' ');
            } else {
                qrReferenceBuffer.insert((CHARS_FIRST_BLOCK + ((blockNr - 1) * CHARS_PER_BLOCK)) + blockNr - 1, ' ');
            }
        }

        return qrReferenceBuffer.toString();
    }

    private static final int[] DIGIT_TABLE = {0, 9, 4, 6, 8, 2, 7, 1, 3, 5};

    public static int modulo10Recursive(final String number) {
        int transfer = 0;
        if (StringUtils.isUnsignedIntegerNumber(number)) {
            for (final char c : number.toCharArray()) {
                final int digit = Character.getNumericValue(c);
                transfer = DIGIT_TABLE[(transfer + digit) % 10];
            }
            return (10 - transfer) % 10;
        }
        throw new IllegalArgumentException("QR reference number must only contain digits");
    }

    /**
     * Normalizes a QR Reference - does the exact opposite of {@link #formatQrReference(String)}
     *
     * @param qrReferenceInput The QR Reference to normalize, may or may not contain whitespaces (e.g. 11 00012 34560 00000 00008 13457)
     * @return The normalized QR Reference (e.g. 110001234560000000000813457), which means the QR reference without any whitespaces or an empty string, if null was passed
     */
    public static String normalizeQrReference(final String qrReferenceInput) {
        return removeWhitespaces(qrReferenceInput);
    }

    /**
     * Creates a normalized QR Reference
     *
     * @param qrReferenceInput A string that may already be a valid QR Reference number or just any number (like invoice nr) that needs to be converted
     * @return The normalized QR Reference (e.g. 110001234560000000000813457)
     */
    public static String createQrReference(final String qrReferenceInput) {
        if (StringUtils.isBlank(qrReferenceInput)) {
            throw new IllegalArgumentException("Input must not be empty");
        }

        final String normalizedQrReference = normalizeQrReference(qrReferenceInput);
        if (isValidQrReference(normalizedQrReference)) {
            return normalizedQrReference;
        } else if (normalizedQrReference.length() == LENGTH) {
            throw new IllegalArgumentException("Input reference length equals QR reference number total length of '" + LENGTH + "' but is not a valid QR reference");
        }

        if (normalizedQrReference.length() > LENGTH) {
            throw new IllegalArgumentException("Invalid QR reference number - total length of '" + LENGTH + "' exceeded");
        } else {
            final String qrReferenceFilledUp = StringUtils.leftPad(normalizedQrReference, LENGTH - 1, '0');
            // Merging the first 26 digits of the QR reference with the correct calculated checksum
            final String newQrReferenceNumber = qrReferenceFilledUp + QRReferenceUtils.modulo10Recursive(qrReferenceFilledUp);
            if (isValidQrReference(newQrReferenceNumber)) {
                return newQrReferenceNumber;
            } else {
                throw new IllegalArgumentException("Invalid QR reference number");
            }
        }
    }

    /**
     * Creates a normalized QR Reference from customer Id and reference number
     *
     * @param customerId       A string representing the banks customer id number
     * @param qrReferenceInput Any number (like invoice nr)
     * @return The normalized QR Reference (e.g. 110001234560000000000813457)
     */
    public static String createQrReference(final String customerId, final String qrReferenceInput) {
        final String cleanCustomerId = removeWhitespaces(customerId);
        final String cleanQrReferenceInput = removeWhitespaces(qrReferenceInput);

        // If no customerId is given, return a conventional qrReferenceNumber
        if (StringUtils.isEmpty(cleanCustomerId)) {
            return createQrReference(cleanQrReferenceInput);
        }

        // Check validity of the qrReferenceInput
        if (StringUtils.isBlank(cleanQrReferenceInput)) {
            throw new IllegalArgumentException("Input must not be empty");
        }

        // Check total length
        if (cleanCustomerId.length() + cleanQrReferenceInput.length() > LENGTH - 1) {
            throw new IllegalArgumentException("Combined length of customer Id and QR reference number cannot exceed " + (LENGTH - 1) + ".");
        }

        // Otherwise use the customerId as prefix, the qrReferenceInput as suffix and fill the rest with 0's.
        final String combinedInput = cleanCustomerId + StringUtils.leftPad(cleanQrReferenceInput, LENGTH - 1 - cleanCustomerId.length(), '0');

        return createQrReference(combinedInput);
    }
}
