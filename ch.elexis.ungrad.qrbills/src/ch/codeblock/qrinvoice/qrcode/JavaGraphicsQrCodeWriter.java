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
import ch.codeblock.qrinvoice.NotYetImplementedException;
import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.graphics.SwissCross;
import ch.codeblock.qrinvoice.image.ImageSupport;
import ch.codeblock.qrinvoice.layout.Dimension;
import ch.codeblock.qrinvoice.layout.DimensionUnitUtils;
import ch.codeblock.qrinvoice.layout.Rect;
import ch.codeblock.qrinvoice.output.QrCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The following information comes from the specification v2.0
 * <p>"The measurements of the Swiss QR Code for printing must always be 46 x 46 mm (without surrounding quiet space) regardless of the Swiss QR Code version."</p>
 * <p>"To increase the recognizability and differentiation for users, the Swiss QR Code created for printout is to be overlaid with a Swiss cross logo measuring 7 x 7 mm."</p>
 */
public class JavaGraphicsQrCodeWriter extends AbstractQrCodeWriter implements IQrCodeWriter {
    private final Logger logger = LoggerFactory.getLogger(JavaGraphicsQrCodeWriter.class);

    public static JavaGraphicsQrCodeWriter create(final QrCodeWriterOptions qrCodeWriterOptions) {
        return new JavaGraphicsQrCodeWriter(qrCodeWriterOptions);
    }

    public JavaGraphicsQrCodeWriter(final QrCodeWriterOptions qrCodeWriterOptions) {
        super(qrCodeWriterOptions);

    }

    public QrCode write(final String qrCodeString) {
        final OutputFormat outputFormat = qrCodeWriterOptions.getOutputFormat();
        final int desiredQrCodeSize = getDesiredOutputQrCodeSize();

        validateQrCodeStringArgument(qrCodeString);
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(5 * 1024);
            final Dimension<Integer> dimension = renderSwissQrCode(outputFormat, qrCodeString, desiredQrCodeSize, baos);
            return new QrCode(outputFormat, baos.toByteArray(), dimension.getWidth(), dimension.getHeight());
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Unexpected exception encountered during SwissQrCode creation", e);
        }
    }

    private int getDesiredOutputQrCodeSize() {
        if (qrCodeWriterOptions.getOutputResolution() != null) {
            final int dpi = qrCodeWriterOptions.getOutputResolution().getDpi();
            final float qrCodeMillimeters = SwissQrCode.QR_CODE_SIZE.getWidth();
            // millimeters to pixels
            return DimensionUnitUtils.millimetersToPointsRounded(qrCodeMillimeters, dpi);
        } else {
            return qrCodeWriterOptions.getDesiredQrCodeSize();
        }
    }

    public BufferedImage writeBufferedImage(final String qrCodeString) {
        final int desiredQrCodeSize = getDesiredOutputQrCodeSize();

        validateQrCodeStringArgument(qrCodeString);
        try {
            return renderSwissQrCodeAsRasterizedGraphic(qrCodeString, desiredQrCodeSize);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new TechnicalException("Unexpected exception encountered during SwissQrCode creation", e);
        }
    }

    private Dimension<Integer> renderSwissQrCode(final OutputFormat outputFormat, final String qrCodeString, final int desiredQrCodeSize, final OutputStream outputStream) throws WriterException, IOException {
        switch (outputFormat) {
            case PNG:
            case GIF:
            case TIFF:
            case BMP:
            case JPEG:
                final BufferedImage qrCodeImage = renderSwissQrCodeAsRasterizedGraphic(qrCodeString, desiredQrCodeSize);
                new ImageSupport().write(outputFormat, outputStream, qrCodeImage);
                return new Dimension<>(qrCodeImage.getWidth(), qrCodeImage.getHeight());
            default:
                throw new NotYetImplementedException("Output Format " + outputFormat + " has not yet been implemented");
        }
    }

    private BufferedImage renderSwissQrCodeAsRasterizedGraphic(final String qrCodeString, final int desiredQrCodeSize) throws WriterException, IOException {
        BufferedImage qrImage = renderQrCode(qrCodeString, desiredQrCodeSize);
        return overlayWithQrCodeLogo(qrImage);
    }

    private BufferedImage renderQrCode(final String qrCodeString, final int desiredQrCodeSize) throws WriterException {
        final QRCode qrCode = createZxingQrCode(qrCodeString);
        validateQrCodeMaxVersion(qrCode);

        final int minimalQrCodeSize = getMinimalQrCodeSize(qrCode);
        final int optimalQrCodeRenderSize = getOptimalQrCodeRenderSize(desiredQrCodeSize, minimalQrCodeSize);

        if (logger.isTraceEnabled()) {
            logger.trace("desiredQrCodeSize: {}", desiredQrCodeSize);
            logger.trace("minimalQrCodeSize: {}", minimalQrCodeSize);
            logger.trace("optimalQrCodeRenderSize: {}", optimalQrCodeRenderSize);
        }

        final QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // Create the ByteMatrix for the QR-Code that encodes the given String
        final BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeString, BarcodeFormat.QR_CODE, optimalQrCodeRenderSize, optimalQrCodeRenderSize, ZxingHints.ENCODING_HINTS);
        final MatrixToImageConfig config = new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);

        final BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
        if (logger.isTraceEnabled()) {
            logger.trace("qrImageWidth: {}", qrImage.getWidth());
            logger.trace("qrImageHeight: {}", qrImage.getHeight());
        }

        return qrImage;
    }

    private BufferedImage overlayWithQrCodeLogo(final BufferedImage qrImage) throws IOException {
        final int qrCodeWidth = qrImage.getWidth();
        final int qrCodeHeight = qrImage.getHeight();

        // Initialize combined image
        Graphics2D g = null;
        try {
            g = (Graphics2D) qrImage.getGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            final Rect<Integer> qrCodeLogoRect = getQrCodeLogoRect(qrCodeWidth);
            final double scale = (double) qrCodeLogoRect.getHeight() / (double) SwissCross.getOrigHeight();

            final AffineTransform previousTransform = g.getTransform();
            final AffineTransform swissCrossTransform = new AffineTransform();
            swissCrossTransform.translate(qrCodeLogoRect.getLeftX(), qrCodeLogoRect.getTopY());
            swissCrossTransform.scale(scale, scale);
            g.setTransform(swissCrossTransform);
            SwissCross.paint(g);
            // reset transform
            g.setTransform(previousTransform);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }

        return qrImage;
    }
}
