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
package ch.codeblock.qrinvoice.qrcode;

import ch.codeblock.qrinvoice.layout.Dimension;

public class SwissQrCode {
    private SwissQrCode() {
    }

    /**
     * Error correction level M must be used according to the spec 5.1 - Error correction level - v2.0
     */
    static final String QR_CODE_ERROR_CORRECTION_LEVEL = "M";

    /**
     * Min QR Code version with error correction level M and binary level M must be used according to the spec 5.2  - v2.0
     */
    public static final int QR_CODE_MIN_VERSION = 5;
    /**
     * The minimum QR Code Modules that corresponds to the {@link #QR_CODE_MIN_VERSION} respecting the error correction and binary level
     */
    public static final int QR_CODE_MIN_MODULES = 37;

    /**
     * Max permitted QR Code version with error correction level M and binary level M must be used according to the spec 5.2  - v2.0
     * It implicitly verifies that max content and modules are not exceeded
     */
    public static final int QR_CODE_MAX_VERSION = 25;
    /**
     * The max QR Code Modules that corresponds to the {@link #QR_CODE_MAX_VERSION} respecting the error correction and binary level
     */
    public static final int QR_CODE_MAX_MODULES = 117;

    /**
     * Size in mm according to the spec 5.4 - Measurements of the Swiss QR Code for printing - v2.0
     */
    public static final Dimension<Float> QR_CODE_SIZE = new Dimension<>(46f, 46f);

    /**
     * Size in mm according to the spec 5.4.2 - Recognition characters - v2.0
     */
    static final Dimension<Float> QR_CODE_LOGO_SIZE = new Dimension<>(7f, 7f);
}
