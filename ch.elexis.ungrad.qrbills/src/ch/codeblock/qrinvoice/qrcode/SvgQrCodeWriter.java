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
import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.layout.Rect;
import ch.codeblock.qrinvoice.output.QrCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;

import java.nio.charset.StandardCharsets;

public class SvgQrCodeWriter extends AbstractQrCodeWriter implements IQrCodeWriter {

    public SvgQrCodeWriter(final QrCodeWriterOptions qrCodeWriterOptions) {
        super(qrCodeWriterOptions);
    }

    @Override
    public QrCode write(final String qrCodeString) {
        validateQrCodeStringArgument(qrCodeString);
        try {
            final QRCode qrCode = createZxingQrCode(qrCodeString);
            validateQrCodeMaxVersion(qrCode);

            final int minimalQrCodeSize = getMinimalQrCodeSize(qrCode);

            final QRCodeWriter qrCodeWriter = new QRCodeWriter();
            final BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeString, BarcodeFormat.QR_CODE, minimalQrCodeSize, minimalQrCodeSize, ZxingHints.ENCODING_HINTS);

            final Rect<Integer> qrLogoCodeRect = getQrCodeLogoRect(minimalQrCodeSize);

            final StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 ").append(bitMatrix.getWidth()).append(" ").append(bitMatrix.getHeight()).append("\" stroke=\"none\">\n");
            sb.append("<style type=\"text/css\">\n");
            sb.append(".black {fill:#000000;}\n");
            sb.append(".white {fill:#FFFFFF;}\n");
            sb.append(".whitestroke {fill:none;stroke:#FFFFFF;stroke-width:1.4357;stroke-miterlimit:10;}\n");
            sb.append("</style>\n");
            sb.append("<defs>\n");
            appendSwissCrossDefinition(sb, qrLogoCodeRect);
            sb.append("</defs>\n");

            appendQrCode(sb, bitMatrix);

            sb.append("<use href=\"#swisscross\" transform=\"translate(").append(qrLogoCodeRect.getLeftX()).append(",").append(qrLogoCodeRect.getTopY()).append(")\" />\n");
            sb.append("</svg>\n");
            return new QrCode(OutputFormat.SVG, sb.toString().getBytes(StandardCharsets.UTF_8), null, null);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Unexpected exception encountered during SwissQrCode creation", e);
        }
    }

    private void appendSwissCrossDefinition(final StringBuilder sb, final Rect<Integer> qrLogoCodeRect) {
        final double scale = (double) qrLogoCodeRect.getHeight() / 19.8;
        sb.append("<g id=\"swisscross\" class=\"swisscrossbox\" transform=\"scale(").append(scale).append(")\">\n")
                .append("<polygon points=\"18.3,0.7 1.6,0.7 0.7,0.7 0.7,1.6 0.7,18.3 0.7,19.1 1.6,19.1 18.3,19.1 19.1,19.1 19.1,18.3 19.1,1.6 19.1,0.7 \"/>\n")
                .append("<rect x=\"8.3\" y=\"4\" class=\"white\" width=\"3.3\" height=\"11\"/>\n")
                .append("<rect x=\"4.4\" y=\"7.9\" class=\"white\" width=\"11\" height=\"3.3\"/>\n")
                .append("<polygon class=\"whitestroke\" points=\"0.7,1.6 0.7,18.3 0.7,19.1 1.6,19.1 18.3,19.1 19.1,19.1 19.1,18.3 19.1,1.6 19.1,0.7 18.3,0.7 1.6,0.7 0.7,0.7 \"/></g>\n");
    }

    private void appendQrCode(final StringBuilder sb, final BitMatrix bitMatrix) {
        sb.append("<path class=\"black\"  d=\"");

        final int width = bitMatrix.getWidth();
        final int height = bitMatrix.getHeight();

        BitArray row = new BitArray(width);
        for (int y = 0; y < height; ++y) {
            row = bitMatrix.getRow(y, row);
            for (int x = 0; x < width; ++x) {
                if (row.get(x)) {
                    sb.append(" M").append(x).append(",").append(y).append("h1v1h-1z");
                }
            }
        }

        // close path
        sb.append("\"/>\n");
    }

}
