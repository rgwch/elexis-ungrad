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

import ch.codeblock.qrinvoice.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ch.codeblock.qrinvoice.util.StringUtils.escapeControlCharacters;

public class ValidationResult implements Serializable {
    private static final long serialVersionUID = 8225257926746356518L;

    private List<ValidationError<Serializable>> errors;

    public <T extends Serializable> void addError(final String parentDataPath, final String dataPath, final T value, final String... msgs) {
        final String completeDataPath = ((dataPath != null) ? parentDataPath + '.' + dataPath : parentDataPath);
        addError(new ValidationError<>(completeDataPath, value, msgs));
    }

    private void addError(final ValidationError<Serializable> error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        // do not accept duplicates
        if (!errors.contains(error)) {
            errors.add(error);
        }
    }

    public ValidationResult includeErrorsFrom(ValidationResult other) {
        if (other != null && other.getErrors() != null) {
            getErrors().addAll(other.getErrors());
        }
        return this;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(errors);
    }

    public boolean isValid() {
        return isEmpty();
    }

    public boolean hasErrors() {
        return !isEmpty();
    }

    /**
     * Checks if there are errors for the passed dataPath
     *
     * @param dataPath the dataPath to check for errors, must not be null
     * @return true, if at least one error is present for the data path
     */
    public boolean hasErrors(final String dataPath) {
        return CollectionUtils.isNotEmpty(errors) && errors.stream().anyMatch(error -> dataPath.equals(error.dataPath));
    }

    /**
     * Checks that there are no errors for a given dataPath
     *
     * @param dataPath the dataPath to check if it is valid (= has no errors)
     * @return true, if there is no error present for the data path
     */
    public boolean isValid(final String dataPath) {
        return !hasErrors(dataPath);
    }

    /**
     * if there are {@link #getErrors()}, throw a {@link ValidationException}, otherwise it is a silent operation
     *
     * @throws ValidationException in case there are validation errors present
     */
    public void throwExceptionOnErrors() throws ValidationException {
        if (hasErrors()) {
            throw new ValidationException(this);
        }
    }

    public List<ValidationError<Serializable>> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public String getValidationErrorSummary() {
        if (hasErrors()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("QrInvoice has validation errors:");
            sb.append(System.lineSeparator());

            appendValidationErrors(sb);

            return sb.toString();
        } else {
            return "No validation errors";
        }
    }
    
    public void appendValidationErrors(final StringBuilder sb) {
        int errorNr = 1;
        for (final ValidationError<?> error : errors) {
            if (errorNr > 1) {
                sb.append(System.lineSeparator());
            }
            sb.append(errorNr++).append(". '").append(error.getDataPath()).append('\'');
            if (error.getValue() instanceof String) {
                sb.append(" has invalid value '").append(escapeControlCharacters((String) error.getValue())).append("'");
            } else if (error.getValue() != null) {
                sb.append(" has invalid value '").append(error.getValue()).append("'");
            } else {
                sb.append(" has invalid value");
            }

            error.appendErrorMessageSummary(sb);
        }
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "errors=" + errors +
                '}';
    }

    public static class ValidationError<T extends Serializable> implements Serializable {
        private static final long serialVersionUID = -8699700959693274449L;

        // only english error messages
        private static final ResourceBundle LABELS = ResourceBundle.getBundle("qrinvoice", Locale.ENGLISH);
        private static final Predicate<String> MESSAGE_KEY_PREDICATE = (str) -> str.startsWith("{{") && str.endsWith("}}");
        private static final Function<String, String> MESSAGE_KEY_EXTRACTOR = (str) -> str.substring(2, str.length() - 2);
        private static final Function<String, String> MESSAGE_RESOLVER = MESSAGE_KEY_EXTRACTOR.andThen(LABELS::getString);
        private static final Function<String, String> TO_MESSAGE = (str) -> {
            if (MESSAGE_KEY_PREDICATE.test(str)) {
                return MESSAGE_RESOLVER.apply(str);
            } else {
                return str;
            }
        };

        private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\r\\n?|\\n");

        private final String dataPath;
        private final T value;
        private final String[] messages;

        public ValidationError(final String dataPath, final T value, final String... messages) {
            this.dataPath = dataPath;
            this.value = value;
            this.messages = messages;
        }

        public String getDataPath() {
            return dataPath;
        }

        public T getValue() {
            return value;
        }

        /**
         * @return an array of all error message keys (keys only, unresolved, unresolved, e.g. `validation.error.invalidCharacters`)
         * @deprecated use {@link #getErrorMessageKeys()} instead
         */
        @Deprecated
        public String[] getErrorMsgKeys() {
            return Arrays.stream(messages).filter(MESSAGE_KEY_PREDICATE).map(MESSAGE_KEY_EXTRACTOR).toArray(String[]::new);
        }

        /**
         * @return an array of all error message keys (keys only, unresolved, e.g. `validation.error.invalidCharacters`)
         */
        public List<String> getErrorMessageKeys() {
            return Arrays.stream(messages).filter(MESSAGE_KEY_PREDICATE).map(MESSAGE_KEY_EXTRACTOR).collect(Collectors.toList());
        }

        /**
         * @return a list of all error messages
         */
        public List<String> getErrorMessages() {
            return Arrays.stream(messages).map(TO_MESSAGE).collect(Collectors.toList());
        }

        protected void appendErrorMessageSummary(StringBuilder sb) {
            final Consumer<String> lineAppender = s -> sb.append(System.lineSeparator()).append("    => ").append(s);
            Arrays.stream(messages)
                    .map(TO_MESSAGE)
                    .map(LINE_BREAK_PATTERN::split)
                    .flatMap(Arrays::stream)
                    .forEach(lineAppender);
        }


        @Override
        public String toString() {
            return "ValidationError{" +
                    "dataPath='" + dataPath + '\'' +
                    ", value=" + value +
                    ", messages='" + Arrays.toString(messages) + '\'' +
                    '}';
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ValidationError<?> that = (ValidationError<?>) o;
            return Objects.equals(dataPath, that.dataPath) &&
                    Objects.equals(value, that.value) &&
                    Arrays.equals(messages, that.messages);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataPath, value, messages);
        }
    }
}
