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
package ch.codeblock.qrinvoice.layout;


import java.util.Objects;
import java.util.function.Function;

/**
 * Dimension of a geometric shape such as a rectangle.
 * <p>
 * <pre>
 *   width  = 5
 * +------------+
 * |            |
 * |            | height = 3
 * |            |
 * +------------+
 * </pre>
 *
 * @param <T> The concrete Number subtype such as Integer, Float...
 */

public class Dimension<T extends Number> {
    private final T width;
    private final T height;

    public Dimension(final T width, final T height) {
        this.width = width;
        this.height = height;
    }

    public T getWidth() {
        return width;
    }

    public T getHeight() {
        return height;
    }

    /**
     * Transforms the Dimension using the given function. This is used in order to translate values from one unit (e.g. millimeters) to another unit (e.g. points)
     * @param transformFunction The function that translates both width and height
     * @return A new Dimension
     */
    public Dimension<T> transform(Function<T, T> transformFunction) {
        return new Dimension<>(transformFunction.apply(width),transformFunction.apply(height));
    }
    
    public Rect<T> toRectangle(final T x, final T y) {
        return Rect.createUsingDimension(x, y, this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Dimension<?> dimension = (Dimension<?>) o;
        return Objects.equals(width, dimension.width) &&
                Objects.equals(height, dimension.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}
