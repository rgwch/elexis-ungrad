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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public enum MimeType {
    PDF("application/pdf", "pdf"),
    PNG("image/png", "png"),
    GIF("image/gif", "gif"),
    JPEG("image/jpeg", "jpeg", "jpg"),
    BMP("image/bmp", "bmp"),
    TIFF("image/tiff", "tiff"),
    SVG("image/svg+xml", "svg"),
    ZIP("application/zip", "zip"),
    CSV("text/csv", "csv"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private static final Logger LOGGER = LoggerFactory.getLogger(MimeType.class);

    private final String mimeType;
    private final String[] fileExtension;

    MimeType(final String mimeType, final String... fileExtension) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension[0];
    }

    public static Optional<MimeType> getByMimeType(final String mimeType) {
        for (final MimeType MimeType : values()) {
            if (MimeType.getMimeType().equalsIgnoreCase(mimeType)) {
                return Optional.of(MimeType);
            }
        }

        LOGGER.info("Encountered unknown / unsupported mimeType={}", mimeType);
        return Optional.empty();
    }


    public static Optional<MimeType> getByFilename(String name) {
        final int beginIndex = name.lastIndexOf(".");
        if (beginIndex != -1) {
            final String extension = name.substring(beginIndex);
            return getByExtension(extension);
        }

        LOGGER.info("Unable to determine mimeType for filename={}", name);

        return Optional.empty();
    }

    public static Optional<MimeType> getByExtension(String extension) {
        if (extension != null && extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        for (MimeType mimeType : MimeType.values()) {
            for (String fileExtension : mimeType.fileExtension) {
                if (fileExtension.equalsIgnoreCase(extension)) {
                    return Optional.of(mimeType);
                }
            }
        }

        LOGGER.info("Unable to determine mimeType for extension={}", extension);
        return Optional.empty();
    }
}
