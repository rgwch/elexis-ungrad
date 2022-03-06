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


import ch.codeblock.qrinvoice.NotYetImplementedException;
import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.infrastructure.IOutputWriterFactory;
import ch.codeblock.qrinvoice.infrastructure.ServiceProvider;
import ch.codeblock.qrinvoice.output.QrCode;

public class QrCodeWriter {
    public static QrCodeWriter create() {
        return new QrCodeWriter();
    }

    public QrCode write(final QrCodeWriterOptions options, final String qrCodeString) {
        final IQrCodeWriter qrCodeWriter = selectQrCodeWriter(options);
        return qrCodeWriter.write(qrCodeString);
    }

    private IQrCodeWriter selectQrCodeWriter(final QrCodeWriterOptions options) {
        final OutputFormat outputFormat = options.getOutputFormat();
        switch (outputFormat) {
            case PDF:
            case PNG:
            case GIF:
            case TIFF:
            case BMP:
            case JPEG:
            case SVG:
                return ServiceProvider.getInstance().get(IOutputWriterFactory.class, options).create(options);
            default:
                throw new NotYetImplementedException("Output Format " + outputFormat + " has not yet been implemented");
        }
    }

}
