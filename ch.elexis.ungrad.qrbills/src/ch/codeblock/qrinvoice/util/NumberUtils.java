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

import ch.codeblock.qrinvoice.NotYetImplementedException;

import java.math.BigDecimal;
import java.util.Objects;

public class NumberUtils {
    private NumberUtils() {
    }

    public static <T extends Number> boolean isZero(final T number) {
        if (Objects.isNull(number)) {
            throw new IllegalArgumentException("only non-null values must be passed");
        }
        if (number instanceof Integer) {
            return number.intValue() == 0;
        } else if (number instanceof Float) {
            return number.floatValue() == 0;
        } else if (number instanceof Double) {
            return number.doubleValue() == 0;
        } else if (number instanceof Long) {
            return number.longValue() == 0;
        } else if (number instanceof Short) {
            return number.shortValue() == 0;
        } else if (number instanceof BigDecimal) {
            return BigDecimal.ZERO.compareTo((BigDecimal) number) == 0;
        } else {
            throw new NotYetImplementedException();
        }
    }
    public static <T extends Number> boolean isNegative(final T number) {
        if (Objects.isNull(number)) {
            throw new IllegalArgumentException("only non-null values must be passed");
        }
        if (number instanceof Integer) {
            return number.intValue() < 0;
        } else if (number instanceof Float) {
            return number.floatValue() < 0;
        } else if (number instanceof Double) {
            return number.doubleValue() < 0;
        } else if (number instanceof Long) {
            return number.longValue() < 0;
        } else if (number instanceof Short) {
            return number.shortValue() < 0;
        } else if (number instanceof BigDecimal) {
            return BigDecimal.ZERO.compareTo((BigDecimal) number) > 0;
        } else {
            throw new NotYetImplementedException();
        }
    }

    /**
     * 
     * @param number
     * @param <T>
     * @return true, if number is positive, including 0 (zero) to be a positive number
     */
    public static <T extends Number> boolean isPositive(final T number) {
        return !isNegative(number);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T subtract(final T from, final T subtract) {
        if (Objects.isNull(from) || Objects.isNull(subtract)) {
            throw new IllegalArgumentException("only non-null values must be passed");
        }
        if (from instanceof Integer) {
            return (T) Integer.valueOf(from.intValue() - subtract.intValue());
        } else if (from instanceof Float) {
            // convert to double via toString in order to prevent precision issues
            // subtract using BigDecimal to prevent precision issues
            final BigDecimal bigDecimalResult = subtract(BigDecimal.valueOf(Double.valueOf(from.toString())), BigDecimal.valueOf(Double.valueOf(subtract.toString())));
            return (T) Float.valueOf(bigDecimalResult.floatValue());
        } else if (from instanceof Double) {
            // subtract using BigDecimal to prevent precision issues
            final BigDecimal bigDecimalResult = subtract(BigDecimal.valueOf((Double) from), BigDecimal.valueOf((Double) subtract));
            return (T) Double.valueOf(bigDecimalResult.doubleValue());
        } else if (from instanceof Long) {
            return (T) Long.valueOf(from.longValue() - subtract.longValue());
        } else if (from instanceof Short) {
            return (T) Short.valueOf((short) (from.shortValue() - subtract.shortValue()));
        } else if (from instanceof BigDecimal) {
            return (T) ((BigDecimal) from).subtract((BigDecimal) subtract);
        } else {
            throw new NotYetImplementedException();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T add(final T a, final T b) {
        if (Objects.isNull(a) || Objects.isNull(b)) {
            throw new IllegalArgumentException("only non-null values must be passed");
        }
        if (a instanceof Integer) {
            return (T) Integer.valueOf(a.intValue() + b.intValue());
        } else if (a instanceof Float) {
            return (T) Float.valueOf(a.floatValue() + b.floatValue());
        } else if (a instanceof Double) {
            return (T) Double.valueOf(a.doubleValue() + b.doubleValue());
        } else if (a instanceof Long) {
            return (T) Long.valueOf(a.longValue() + b.longValue());
        } else if (a instanceof Short) {
            return (T) Short.valueOf((short) (a.shortValue() + b.shortValue()));
        } else if (a instanceof BigDecimal) {
            return (T) ((BigDecimal) a).add((BigDecimal) b);
        } else {
            throw new NotYetImplementedException();
        }
    }
}
