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

import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import com.google.zxing.EncodeHintType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

class ZxingHints {
    private ZxingHints() {
    }

    static final Map<EncodeHintType, Object> ENCODING_HINTS;

    static {
        Map<EncodeHintType, Object> map = new EnumMap<>(EncodeHintType.class);
        map.put(EncodeHintType.ERROR_CORRECTION, SwissQrCode.QR_CODE_ERROR_CORRECTION_LEVEL);
        // no margin, gets added later on in the image/rendering. is much easier because the spec says it has to be 5mm (3.5.2 - Swiss QR Code section - v2.0)
        map.put(EncodeHintType.MARGIN, 0);
        map.put(EncodeHintType.CHARACTER_SET, SwissPaymentsCode.ENCODING.name());

        ENCODING_HINTS = Collections.unmodifiableMap(map);
    }
}
