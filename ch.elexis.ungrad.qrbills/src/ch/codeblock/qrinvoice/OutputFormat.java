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
package ch.codeblock.qrinvoice;

import java.util.Optional;

public enum OutputFormat {
    PDF(MimeType.PDF.getMimeType(), MimeType.PDF.getFileExtension()),
    PNG(MimeType.PNG.getMimeType(), MimeType.PNG.getFileExtension()),
    GIF(MimeType.GIF.getMimeType(), MimeType.GIF.getFileExtension()),
    JPEG(MimeType.JPEG.getMimeType(), MimeType.JPEG.getFileExtension()),
    BMP(MimeType.BMP.getMimeType(), MimeType.BMP.getFileExtension()),
    TIFF(MimeType.TIFF.getMimeType(), MimeType.TIFF.getFileExtension()),
    SVG(MimeType.SVG.getMimeType(), MimeType.SVG.getFileExtension()),
    ZIP(MimeType.ZIP.getMimeType(), MimeType.ZIP.getFileExtension());

    private final String mimeType;
    private final String fileExtension;

    OutputFormat(final String mimeType, final String fileExtension) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public MimeType toMimeType() {
        return MimeType.getByMimeType(mimeType).orElse(null);
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static Optional<OutputFormat> getByMimeType(final String mimeType) {
        for (final OutputFormat outputFormat : values()) {
            if (outputFormat.getMimeType().equalsIgnoreCase(mimeType)) {
                return Optional.of(outputFormat);
            }
        }

        return Optional.empty();
    }
}
