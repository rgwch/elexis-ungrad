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

import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.layout.Rect;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import static ch.codeblock.qrinvoice.model.SwissPaymentsCode.SWISS_PAYMENTS_CODE_MAX_LENGTH;
import static ch.codeblock.qrinvoice.qrcode.SwissQrCode.*;

public abstract class AbstractQrCodeWriter implements IQrCodeWriter {
    protected static final ErrorCorrectionLevel ERROR_CORRECTION_LEVEL = ErrorCorrectionLevel.valueOf(QR_CODE_ERROR_CORRECTION_LEVEL);
    protected final QrCodeWriterOptions qrCodeWriterOptions;

    protected AbstractQrCodeWriter(final QrCodeWriterOptions qrCodeWriterOptions) {
        this.qrCodeWriterOptions = qrCodeWriterOptions;

        validateOutputFormat(qrCodeWriterOptions.getOutputFormat());
        validateDesiredQrCodeSizeOrResolution(qrCodeWriterOptions);
    }

    @Override
    public int getQrCodeImageSize() {
        throw new TechnicalException("Not implemented. Shouldn't be called");
    }

    private void validateDesiredQrCodeSizeOrResolution(final QrCodeWriterOptions qrCodeWriterOptions) {
        if (qrCodeWriterOptions.getDesiredQrCodeSize() > QrCodeWriterOptions.DESIRED_QR_CODE_SIZE_UNSET && qrCodeWriterOptions.getOutputResolution() != null) {
            throw new ValidationException("Either specify desiredQrCodeSize OR outputResolution");
        }

        if (qrCodeWriterOptions.getDesiredQrCodeSize() > 10_000) {
            throw new ValidationException("QrCode size is limited to 10'000 pixels. Please consider memory usage and file sizes when creating high resolution qr codes.");
        }
    }

    private void validateOutputFormat(final OutputFormat outputFormat) {
        if (outputFormat == null) {
            throw new TechnicalException("Output format must not be null");
        }
    }


    protected int getOptimalQrCodeRenderSize(final int desiredQrCodeSize, final int minimalQrCodeSize) {
        if (minimalQrCodeSize >= desiredQrCodeSize) {
            return minimalQrCodeSize;
        } else {
            return minimalQrCodeSize * Math.floorDiv(desiredQrCodeSize, minimalQrCodeSize);
        }
    }

    protected void validateQrCodeStringArgument(final String qrCodeString) {
        if (qrCodeString == null) {
            throw new TechnicalException("QrCode String must not be null");
        }
        if (qrCodeString.length() > SWISS_PAYMENTS_CODE_MAX_LENGTH) {
            // make sure code never exceeds max length
            throw new ValidationException("The maximum Swiss QR Code data content permitted is 997 characters (including the element separators). Encountered " + qrCodeString.length() + " characters");
        }
    }

    protected void validateQrCodeMaxVersion(final QRCode qrCode) {
        if (qrCode.getVersion().getVersionNumber() > QR_CODE_MAX_VERSION) {
            // make sure code never exceeds max length
            throw new ValidationException("The maximal qr code version permitted is " + QR_CODE_MAX_VERSION + ". Encountered version " + qrCode.getVersion());
        }
    }

    protected int getMinimalQrCodeSize(final QRCode qrCode) {
        return qrCode.getMatrix().getWidth(); // height/width is the number of modules and corresponds to the qr code version
    }

    protected QRCode createZxingQrCode(final String qrCodeString) throws WriterException {
        return Encoder.encode(qrCodeString, ERROR_CORRECTION_LEVEL, ZxingHints.ENCODING_HINTS);
    }


    protected Rect<Integer> getQrCodeLogoRect(final int qrCodeSize) {
        // Load logo image
        final int qrLogoWidth = Math.floorDiv(qrCodeSize * QR_CODE_LOGO_SIZE.getWidth().intValue(), QR_CODE_SIZE.getWidth().intValue());
        final int qrLogoHeight = Math.floorDiv(qrCodeSize * QR_CODE_LOGO_SIZE.getHeight().intValue(), QR_CODE_SIZE.getHeight().intValue());
        // Calculate the delta height and width between QR code and logo
        final int deltaWidth = qrCodeSize - qrLogoWidth;
        final int deltaHeight = qrCodeSize - qrLogoHeight;

        // Write logo into combine image at position (deltaWidth / 2) and
        // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
        // the same space for the logo to be centered
        final int qrLogoPositionX = Math.round(deltaWidth / 2.0f);
        final int qrLogoPositionY = Math.round(deltaHeight / 2.0f);
        return Rect.create(qrLogoPositionX, qrLogoPositionY - qrLogoHeight, qrLogoPositionX + qrLogoWidth, qrLogoPositionY);
    }
}
