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

import ch.codeblock.qrinvoice.BaseException;
import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.image.ImageSupport;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class QrCodeReader {
    private static final Map<DecodeHintType, Object> hintMap;
    public static final String QR_CODE_NOT_FOUND_MSG = "QR Code could not be found in the given image";
    public static final String UNEXPECTED_EXCEPTION_MSG = "Unexpected exception encountered during read of a SwissQrCode";

    static {
        Map<DecodeHintType, Object> map = new EnumMap<>(DecodeHintType.class);
        map.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singleton(BarcodeFormat.QR_CODE));
        map.put(DecodeHintType.CHARACTER_SET, SwissPaymentsCode.ENCODING.name());
        map.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        map.put(DecodeHintType.ALLOWED_LENGTHS, SwissPaymentsCode.SWISS_PAYMENTS_CODE_MAX_LENGTH + 300); // max length + some additional bytes to make sure all codes, even those which are a bit too long, are picked up.
        hintMap = Collections.unmodifiableMap(map);
    }

    public static QrCodeReader create() {
        return new QrCodeReader();
    }

    public String readQRCode(final BufferedImage image) {
        return internalRead(image);
    }

    public String readQRCode(final byte[] qrImageInput) {
        return readQRCode(new ByteArrayInputStream(qrImageInput));
    }

    public String readQRCode(final InputStream qrImageInput) {
        return internalRead(new ImageSupport().read(qrImageInput));
    }

    private String internalRead(final BufferedImage image) {
        try {
            return new QRCodeReader()
                    .decode(getBinaryBitmap(image), hintMap)
                    .getText();
        } catch (BaseException e) {
            throw e;
        } catch (NotFoundException e) {
            throw new DecodeException(QR_CODE_NOT_FOUND_MSG, e);
        } catch (Exception e) {
            throw new TechnicalException(UNEXPECTED_EXCEPTION_MSG, e);
        }
    }

    public List<String> readQRCodes(final BufferedImage image) {
        return internalReads(image);
    }

    public List<String> readQRCodes(final byte[] qrImageInput) {
        return readQRCodes(new ByteArrayInputStream(qrImageInput));
    }

    public List<String> readQRCodes(final InputStream qrImageInput) {
        return internalReads(new ImageSupport().read(qrImageInput));
    }

    private List<String> internalReads(final BufferedImage image) {
        try {
            final Result[] qrCodeResults = new QRCodeMultiReader().decodeMultiple(getBinaryBitmap(image), hintMap);
            return Arrays.stream(qrCodeResults)
                    .map(Result::getText)
                    .collect(Collectors.toList());
        } catch (BaseException e) {
            throw e;
        } catch (NotFoundException e) {
            throw new DecodeException(QR_CODE_NOT_FOUND_MSG, e);
        } catch (Exception e) {
            throw new TechnicalException(UNEXPECTED_EXCEPTION_MSG, e);
        }
    }


    private BinaryBitmap getBinaryBitmap(final BufferedImage image) {
        return new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
    }


}
