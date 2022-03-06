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

import ch.codeblock.qrinvoice.config.SystemProperties;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.codeblock.qrinvoice.model.SwissPaymentsCode.VALID_CHARACTERS;

public class ValidationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("qrinvoice", Locale.ENGLISH);

    private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile(String.format("([%s]*)", Pattern.quote(SwissPaymentsCode.VALID_CHARACTERS)));
    private static final Pattern INVALID_CHARACTER_SEQUENCE_PATTERN = Pattern.compile(String.format("([^%s]+)", Pattern.quote(VALID_CHARACTERS)));

    // CharSequence
    public static void validateLength(final String str, final int min, final int max, final Consumer<String> validationErrorCallback) {
        validateLength(str, min, max, false, validationErrorCallback);
    }

    public static void validateOptionalLength(final String str, final int min, final int max, final Consumer<String> validationErrorCallback) {
        validateLength(str, min, max, true, validationErrorCallback);
    }

    private static void validateLength(final String str, final int min, final int max, final boolean optional, final Consumer<String> validationErrorCallback) {
        if (!validateLength(str, min, max, optional)) {
            validationErrorCallback.accept(str);
        }
    }

    public static boolean validateLength(final String str, final int min, final int max, final boolean optional) {
        // consider empty string only if min is greater than 0
        if (str == null || (str.length() == 0 && min > 0)) {
            return optional;
        }

        final int length = str.length();
        return (min <= length && length <= max);
    }

    public static void validateEmpty(final String value, final Consumer<String> validationErrorCallback) {
        if (StringUtils.isNotEmpty(value)) {
            validationErrorCallback.accept(value);
        }
    }

    public static void validateString(final String value, final BiConsumer<String, String[]> validationErrorCallback) {
        validateTrimmed(value, validationErrorCallback);
        validateCharacters(value, validationErrorCallback);
    }

    private static void validateTrimmed(final String value, final BiConsumer<String, String[]> validationErrorCallback) {
        if (StringUtils.isNotEmpty(value) && StringUtils.isTrimmable(value)) {
            if (System.getProperty(SystemProperties.STRICT_VALIDATION) != null) {
                // add validation error only in strict mode, because there might be whitespaces when scanning other QR codes
                validationErrorCallback.accept(value, new String[]{"{{validation.error.untrimmedInput}}", null});
            } else {
                logger.warn("Input '{}' should be trimmed. {}", value, BUNDLE.getString("validation.error.untrimmedInput"));
            }
        }
    }

    public static void validateCharacters(final String value, final BiConsumer<String, String[]> validationErrorCallback) {
        final CharacterValidationResult characterValidationResult = validateCharacters(value);
        if (characterValidationResult.hasInvalidCharacters()) {
            final String summary = characterValidationResult.getSummary();
            final String permittedCharactersMsg = String.format("Permitted characters are: '%s'", SwissPaymentsCode.VALID_CHARACTERS);
            validationErrorCallback.accept(value, new String[]{summary, "{{validation.error.invalidCharacters}}", permittedCharactersMsg});
        }
    }


    public static CharacterValidationResult validateCharacters(final String input) {
        final CharacterValidationResult characterValidationResult = new CharacterValidationResult(input);

        if (StringUtils.isNotEmpty(input) && !VALID_CHARACTERS_PATTERN.matcher(input).matches()) {
            final Matcher matcher = INVALID_CHARACTER_SEQUENCE_PATTERN.matcher(input);
            while (matcher.find()) {
                final String invalidCharSequence = matcher.group();
                final int index = matcher.start();
                characterValidationResult.addInvalidCharacterSequence(new InvalidCharacterSequence(input, invalidCharSequence, index));
            }
        }

        return characterValidationResult;
    }

    // Number range

    public static void validateRange(final int number, final int min, final int max, final Consumer<Integer> validationErrorCallback) {
        if (min > number || number > max) {
            validationErrorCallback.accept(number);
        }
    }

    // BigDecimal range
    public static void validateRange(final BigDecimal number, final BigDecimal min, final BigDecimal max, final Consumer<BigDecimal> validationErrorCallback) {
        validateRange(number, min, max, false, validationErrorCallback);
    }

    public static void validateOptionalRange(final BigDecimal number, final BigDecimal min, final BigDecimal max, final Consumer<BigDecimal> validationErrorCallback) {
        validateRange(number, min, max, true, validationErrorCallback);
    }

    private static void validateRange(final BigDecimal number, final BigDecimal min, final BigDecimal max, final boolean optional, final Consumer<BigDecimal> validationErrorCallback) {
        if (!validateRange(number, min, max, optional)) {
            validationErrorCallback.accept(number);
        }
    }

    public static boolean validateRange(final BigDecimal number, final BigDecimal min, final BigDecimal max, final boolean optional) {
        // consider empty string only if min is greater than 0
        if (number == null) {
            return optional;
        }

        final boolean greaterOrEqualThanMin = number.compareTo(min) >= 0;
        final boolean lessOrEqualThanMax = number.compareTo(max) <= 0;
        return greaterOrEqualThanMin && lessOrEqualThanMax;
    }

    public static <T> void validateNull(final T value, final Consumer<T> validationErrorCallback) {
        if (value != null) {
            validationErrorCallback.accept(value);
        }
    }
    
    public static <T> void validateNotNull(final T value, final Consumer<T> validationErrorCallback) {
        if (value == null) {
            validationErrorCallback.accept(value);
        }
    }

    public static <T> void validateTrue(final T value, boolean valid, final Consumer<T> validationErrorCallback) {
        if (!valid) {
            validationErrorCallback.accept(value);
        }
    }
    
    private ValidationUtils() {}
}
