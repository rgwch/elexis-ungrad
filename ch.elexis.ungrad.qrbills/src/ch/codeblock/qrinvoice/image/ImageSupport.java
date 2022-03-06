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
package ch.codeblock.qrinvoice.image;


import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.TechnicalException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class ImageSupport {
    public BufferedImage read(final InputStream is) {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            return ImageIO.read(bis);
        } catch (IOException e) {
            throw new TechnicalException("error while trying to read Image from InputStream", e);
        }
    }

    public void write(final OutputFormat outputFormat, final OutputStream outputStream, final BufferedImage bufferedImage) throws IOException {
        switch (outputFormat) {
            case TIFF:
                this.writeTiff(outputFormat, outputStream, bufferedImage);
                break;
            case JPEG:
                this.writeJpeg(outputFormat, outputStream, bufferedImage);
                break;
            case BMP: // explicit fall through
            case PNG: // explicit fall through
            case GIF: // explicit fall through
            default:
                this.writeSimple(outputFormat, outputStream, bufferedImage);
        }
    }

    private void writeSimple(final OutputFormat outputFormat, final OutputStream outputStream, final BufferedImage bufferedImage) throws IOException {
        if (!ImageIO.write(bufferedImage, outputFormat.name(), outputStream)) {
            throw new TechnicalException("ImageIO: no appropriate writer is found for outputFormat=" + outputFormat.name());
        }
    }

    private void writeJpeg(final OutputFormat outputFormat, final OutputStream outputStream, final BufferedImage bufferedImage) throws IOException {
        final ImageWriter writer = selectImageWriter(outputFormat);
        try (final ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);

            final ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1f); // 100%, best quality

            final IIOImage iioImage = new IIOImage(bufferedImage, null, null);
            writer.write(null, iioImage, param);
        }
    }
    private void writeTiff(final OutputFormat outputFormat, final OutputStream outputStream, final BufferedImage bufferedImage) throws IOException {
        final ImageWriter writer = selectImageWriter(outputFormat);
        try (final ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);

            final ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType("LZW");

            final IIOImage iioImage = new IIOImage(bufferedImage, null, null);
            writer.write(null, iioImage, param);
        }
    }

    private ImageWriter selectImageWriter(final OutputFormat outputFormat) {
        final Iterator it = ImageIO.getImageWritersByFormatName(outputFormat.name().toLowerCase());
        if (it != null && it.hasNext()) {
            return (ImageWriter) it.next();
        } else {
            throw new TechnicalException("Unable to find ImageWriter for " + outputFormat.name());
        }
    }
}
