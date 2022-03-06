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

import ch.codeblock.qrinvoice.model.SwissPaymentsCode;

import java.math.BigInteger;
import java.util.stream.Collectors;

import static ch.codeblock.qrinvoice.util.StringUtils.removeWhitespaces;

/**
 * Utility for working with IBAN numbers. It implements the validation algorithm according to ISO-13616 (International bank account number).
 */
public final class IbanUtils {
    private IbanUtils() {
    }

    private static final int CHARS_PER_BLOCK = 4;

    /**
     * Formats the given IBAN
     *
     * @param ibanInput The IBAN to format (e.g. CH3908704016075473007)
     * @return The formatted IBAN (blocks of 4 chars, e.g. CH39 0870 4016 0754 7300 7) or an empty string in case of null. Never returns null
     */
    public static String formatIban(final String ibanInput) {
        if (ibanInput == null) {
            return "";
        }

        final String iban = normalizeIBAN(ibanInput);
        final StringBuilder ibanBuffer = new StringBuilder(iban);

        final int numberOfBlocks = (int) Math.ceil((double) iban.length() / (double) CHARS_PER_BLOCK);
        for (int blockNr = 1; blockNr < numberOfBlocks; blockNr++) {
            ibanBuffer.insert((blockNr * CHARS_PER_BLOCK) + blockNr - 1, ' ');
        }

        return ibanBuffer.toString();
    }

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 34;
    private static final int MAX_LENGTH_CH_LI = 21; // IBANs in switzerland and liechtenstein are limited to 21 chars
    private static final BigInteger MOD_97 = BigInteger.valueOf(97);
    private static final BigInteger MOD_97_REMAINDER = BigInteger.ONE;

    /**
     * Checks if the given IBAN is valid or not
     *
     * @param ibanInput           The IBAN to validate
     * @param validateCountryCode true, if the country code should be validated. If true, it checks that only supported country codes are considered valid
     * @return true, if the given IBAN number is valid, false otherwise
     * @see <a href="https://www.iso.org/standard/41031.html">ISO 13616-1:2007</a>
     * @see <a href="https://en.wikipedia.org/wiki/International_Bank_Account_Number#Validating_the_IBAN">Validating_the_IBAN</a>
     */
    public static boolean isValidIBAN(final String ibanInput, final boolean validateCountryCode) {
        if (ibanInput == null) {
            return false;
        }

        // first, remove all whitespaces
        final String iban = normalizeIBAN(ibanInput);

        // 1. Check that the total IBAN length is correct as per the country. If not, the IBAN is invalid
        // --> simple, non-country based check of the iban length
        final int len = iban.length();
        if (len < MIN_LENGTH || len > MAX_LENGTH) {
            return false;
        }
        
        // switzerland / liechtenstein
        if(validateCountryCode(iban) && len > MAX_LENGTH_CH_LI) {
            return false;
        }

        // 2. Move the four initial characters to the end of the string
        // --> CH3908704016075473007 -> 08704016075473007CH39
        final String ibanRearranged = iban.substring(4) + iban.substring(0, 4);

        // 3. Replace each letter in the string with two digits, thereby expanding the string, where A = 10, B = 11, ..., Z = 35.
        final String numericIBAN = ibanRearranged.chars()
                .map(Character::getNumericValue)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        // 4. Interpret the string as a decimal integer and compute the remainder of that number on division by 97
        try {
            final BigInteger ibanNumber = new BigInteger(numericIBAN);
            final boolean ibanValid = ibanNumber.mod(MOD_97).equals(MOD_97_REMAINDER);

            // 5. validate country code
            if (ibanValid && validateCountryCode) {
                // only if IBAN is valid perform country code verification
                return validateCountryCode(iban);
            } else {
                // just return the result
                return ibanValid;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateCountryCode(final String iban) {
        final String countryCode = iban.substring(0, 2);
        return SwissPaymentsCode.SUPPORTED_IBAN_COUNTRIES.contains(countryCode);
    }

    /**
     * Start of the QR IID (Instituts-Identifikation) range - see https://www.iso-20022.ch/lexikon/qr-iban/ for further details
     */
    private static final int QR_IID_RANGE_START = 30000;
    /**
     * End of the QR IID (Instituts-Identifikation) range - see https://www.iso-20022.ch/lexikon/qr-iban/ for further details
     */
    private static final int QR_IID_RANGE_END = 31999;

    /**
     * Validates a string and checks, if it as valid QR-IBAN, which means a valid swiss / liechtenstein IBAN number with an IID between {@value #QR_IID_RANGE_START} and {@value #QR_IID_RANGE_END}
     *
     * @param ibanInput The IBAN to validate for QR-IBAN conformance
     * @return true, if the given IBAN number is a valid QR-IBAN, false otherwise
     */
    // currently this can lead to validate the iban twice, maybe this should be refactored in order to parse the iban and work on an IBAN object to avoid double validation and parsing 
    public static boolean isQrIBAN(final String ibanInput) {
        if (ibanInput == null) {
            return false;
        }

        // first, remove all whitespaces
        final String iban = normalizeIBAN(ibanInput);
        if (!isValidIBAN(iban, true)) {
            return false;
        }

        // IBAN: CH3181239000001245689
        //
        // CH	2-stelliger Ländercode / CH für die Schweiz
        // 31	2-stellige Prüfziffer, pro Konto und Bank individuell
        // 81239	Bank-Clearing-Nummer bzw. IID oder QR-IID, zur eindeutigen Identifizierung der kontoführenden Bank des Zahlungsempfängers (pro Bank individuell)
        // 000001245689	12-stellige Kontonummer des Zahlungsempfängers, wo nötig mit führenden Nullen auf 12 Stellen ergänzt
        final int iid = Integer.parseInt(iban.substring(4, 9));

        // check that IID is within the special QR-IID range
        return (QR_IID_RANGE_START <= iid && iid <= QR_IID_RANGE_END);
    }

    /**
     * Normalizes an IBAN - does the exact opposite of {@link #formatIban(String)}
     *
     * @param ibanInput The IBAN to normalize, may or may not contain whitespaces (e.g. CH39 0870 4016 0754 7300 7)
     * @return The normalized IBAN (e.g. CH3908704016075473007), which means the IBAN without any whitespaces or an empty string, if null was passed
     */
    public static String normalizeIBAN(final String ibanInput) {
        if (ibanInput == null) {
            return "";
        }
        return removeWhitespaces(ibanInput);
    }

    /**
     * Normalizes an IBAN - does the exact opposite of {@link #formatIban(String)}
     *
     * @param ibanInput The IBAN to normalize, may or may not contain whitespaces (e.g. CH39 0870 4016 0754 7300 7)
     * @return The normalized IBAN (e.g. CH3908704016075473007), which means the IBAN without any whitespaces or an empty string, if null was passed
     * @deprecated use {@link #normalizeIBAN(String)} instead
     */
    @Deprecated
    public static String normalize(final String ibanInput) {
        return normalizeIBAN(ibanInput);
    }
}
