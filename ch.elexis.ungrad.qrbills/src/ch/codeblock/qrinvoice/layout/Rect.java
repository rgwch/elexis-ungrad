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

import ch.codeblock.qrinvoice.util.NumberUtils;

import java.util.Objects;

/**
 * A Rect represents a rectangle with a specific width and height, including a specific location in a bottom left oriented coordinate system.
 * <p>
 * <pre>
 * ll = lower left corner
 * ul = upper left corner
 * ur = upper right corner
 * lr = lower right corner
 *
 * width  = 5
 * height = 3
 *
 * ul (0,3)              (5,3) ur
 *         +------------+
 *         |            |
 *         |            |
 *         |            |
 *         +------------+
 * ll (0,0)              (5,0) lr
 * </pre>
 *
 * @param <T> The concrete Number subtype such as Integer, Float...
 */
public class Rect<T extends Number> extends Dimension<T> {

    /**
     * the lower left x-coordinate.
     */
    private final T llx;

    /**
     * the lower left y-coordinate.
     */
    private final T lly;

    /**
     * the upper right x-coordinate.
     */
    private final T urx;

    /**
     * the upper right y-coordinate.
     */
    private final T ury;

    /**
     * Constructs a <CODE>Rect</CODE> -object.
     *
     * @param llx lower left x
     * @param lly lower left y
     * @param urx upper right x
     * @param ury upper right y
     */
    private Rect(final T llx, final T lly, final T urx, final T ury) {
        this(llx, lly, urx, ury, NumberUtils.subtract(urx, llx), NumberUtils.subtract(ury, lly));
    }

    /**
     * Constructs a <CODE>Rect</CODE> -object.
     *
     * @param llx lower left x
     * @param lly lower left y
     * @param urx upper right x
     * @param ury upper right y
     */
    private Rect(final T llx, final T lly, final T urx, final T ury, final T width, final T height) {
        super(width, height);
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }


    /**
     * Returns the lower left x-coordinate.
     *
     * @return the lower left x-coordinate
     */
    public T getLowerLeftX() {
        return llx;
    }

    /**
     * Returns the upper right x-coordinate.
     *
     * @return the upper right x-coordinate
     */
    public T getUpperRightX() {
        return urx;
    }


    /**
     * Returns the upper right y-coordinate.
     *
     * @return the upper right y-coordinate
     */
    public T getUpperRightY() {
        return ury;
    }


    /**
     * Returns the lower left y-coordinate.
     *
     * @return the lower left y-coordinate
     */
    public T getLowerLeftY() {
        return lly;
    }

    /**
     * @return the left x coordinate of the Rect, equals {@link #getLowerLeftX()}
     */
    public T getLeftX() {
        return getLowerLeftX();
    }

    /**
     * @return the right x coordinate of the Rect, equals {@link #getUpperRightX()}
     */
    public T getRightX() {
        return getUpperRightX();
    }

    /**
     * @return the lower y coordinate of the Rect, equals {@link #getLowerLeftY()}
     */
    public T getBottomY() {
        return getLowerLeftY();
    }

    /**
     * @return the upper y coordinate of the Rect, equals {@link #getUpperRightY()}
     */
    public T getTopY() {
        return getUpperRightY();
    }

    public Rect<T> move(final Point<T> offset) {
        // shortcut, do nothgin if offset point is (0,0)
        if(offset.isZeroPoint()) {
            return this;
        }
        return move(offset.getX(), offset.getY());
    }

    public Rect<T> move(final T offsetX, final T offsetY) {
        return Rect.createUsingDimension(NumberUtils.add(llx, offsetX), NumberUtils.add(lly, offsetY), getWidth(), getHeight());
    }

    public static <T extends Number> Rect<T> createUsingDimension(final T llx, final T lly, final Dimension<T> dimension) {
        return createUsingDimension(llx, lly, dimension.getWidth(), dimension.getHeight());
    }

    public static <T extends Number> Rect<T> createUsingDimension(final T llx, final T lly, final T width, final T height) {
        return Rect.create(llx, lly, NumberUtils.add(llx, width), NumberUtils.add(lly, height), width, height);
    }

    public static <T extends Number> Rect<T> create(final T llx, final T lly, final T urx, final T ury) {
        return new Rect<>(llx, lly, urx, ury);
    }

    private static <T extends Number> Rect<T> create(final T llx, final T lly, final T urx, final T ury, final T width, final T height) {
        return new Rect<>(llx, lly, urx, ury, width, height);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Rect<?> rect = (Rect<?>) o;
        return Objects.equals(llx, rect.llx) &&
                Objects.equals(lly, rect.lly) &&
                Objects.equals(urx, rect.urx) &&
                Objects.equals(ury, rect.ury);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), llx, lly, urx, ury);
    }
}
