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

public class DimensionUnitUtils {

    private static final float MILLIMETERS_PER_INCH = 25.4f;

    private DimensionUnitUtils() {
    }

    public static Dimension<Integer> millimetersToPointsRounded(Dimension<Float> dimensionInMillimeters, int dpi) {
        return new Dimension<>(millimetersToPointsRounded(dimensionInMillimeters.getWidth(), dpi), millimetersToPointsRounded(dimensionInMillimeters.getHeight(), dpi));
    }
    
    public static int millimetersToPointsRounded(float value, int dpi) {
        return Math.round(millimetersToPoints(value, dpi));
    }

    public static float millimetersToPoints(float value, int dpi) {
        return inchesToPoints(millimetersToInches(value), dpi);
    }

    @Deprecated
    public static float millimetersToPoints(float value) {
        return inchesToPoints(millimetersToInches(value));
    }

    public static float millimetersToInches(float value) {
        return value / MILLIMETERS_PER_INCH;
    }

    @Deprecated
    public static float inchesToPoints(float value) {
        return inchesToPoints(value, 72);
    }

    public static float inchesToPoints(float value, int dpi) {
        return value * dpi;
    }

    public static int pointsToPixels(final int points, int dpi) {
        return points * dpi / 72;
    }

    public static int pointsToPixels(final float points, int dpi) {
        return Math.round(points * dpi / 72);
    }


}
