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

import ch.codeblock.qrinvoice.infrastructure.IDocumentScanner;
import ch.codeblock.qrinvoice.infrastructure.IDocumentScannerFactory;
import ch.codeblock.qrinvoice.infrastructure.ServiceProvider;
import ch.codeblock.qrinvoice.model.QrInvoice;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class QrInvoiceDocumentScanner implements IDocumentScanner {
    private final IDocumentScanner documentScannerDelegate;

    private QrInvoiceDocumentScanner(final IDocumentScanner documentScanner) {
        this.documentScannerDelegate = documentScanner;
    }

    /**
     * @param mimeType e.g. application/pdf, image/png ...
     */
    public static QrInvoiceDocumentScanner create(final String mimeType) {
        return create(MimeType.getByMimeType(mimeType).orElseThrow(() -> new NotYetImplementedException("Mime Type " + mimeType + " has not yet been implemented")));
    }

    public static QrInvoiceDocumentScanner create(final MimeType mimeType) {
        switch (mimeType) {
            case PDF:
            case PNG:
            case GIF:
            case TIFF:
            case BMP:
            case JPEG:
                return new QrInvoiceDocumentScanner(ServiceProvider.getInstance().get(IDocumentScannerFactory.class, mimeType).create());
            default:
                throw new NotYetImplementedException("Mime Type " + mimeType + " has not yet been implemented");
        }
    }

    @Override
    public List<QrInvoice> scanDocumentForAllSwissQrCodes(final byte[] document) throws IOException {
        return documentScannerDelegate.scanDocumentForAllSwissQrCodes(document);
    }

    @Override
    public List<QrInvoice> scanDocumentForAllSwissQrCodes(final InputStream inputStream) throws IOException {
        return documentScannerDelegate.scanDocumentForAllSwissQrCodes(inputStream);
    }

    @Override
    public List<QrInvoice> scanDocumentForAllSwissQrCodes(final byte[] document, final int pageNr) throws IOException {
        return documentScannerDelegate.scanDocumentForAllSwissQrCodes(document, pageNr);
    }

    @Override
    public List<QrInvoice> scanDocumentForAllSwissQrCodes(final InputStream inputStream, final int pageNr) throws IOException {
        return documentScannerDelegate.scanDocumentForAllSwissQrCodes(inputStream, pageNr);
    }

    @Override
    public Optional<QrInvoice> scanDocumentUntilFirstSwissQrCode(final byte[] document) throws IOException {
        return documentScannerDelegate.scanDocumentUntilFirstSwissQrCode(document);
    }

    @Override
    public Optional<QrInvoice> scanDocumentUntilFirstSwissQrCode(final InputStream inputStream) throws IOException {
        return documentScannerDelegate.scanDocumentUntilFirstSwissQrCode(inputStream);
    }

    @Override
    public Optional<QrInvoice> scanDocumentUntilFirstSwissQrCode(final byte[] document, final int pageNr) throws IOException {
        return documentScannerDelegate.scanDocumentUntilFirstSwissQrCode(document, pageNr);
    }

    @Override
    public Optional<QrInvoice> scanDocumentUntilFirstSwissQrCode(final InputStream inputStream, final int pageNr) throws IOException {
        return documentScannerDelegate.scanDocumentUntilFirstSwissQrCode(inputStream, pageNr);
    }
}
