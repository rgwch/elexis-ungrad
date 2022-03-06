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

import ch.codeblock.qrinvoice.model.ParseException;
import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.qrcode.QrCodeReader;
import ch.codeblock.qrinvoice.qrcode.SwissQrCodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class QrInvoiceCodeScanner {
    private static final String UNEXPECTED_EXCEPTION_DURING_PARSE_MSG = "Unexpected exception occurred during parsing of QrCodeImage as QrInvoice";

    public static QrInvoiceCodeScanner create() {
        return new QrInvoiceCodeScanner();
    }

    private final Logger logger = LoggerFactory.getLogger(QrInvoiceCodeScanner.class);

    public QrInvoice scan(final byte[] qrImageInput) throws ParseException {
        try {
            return extractOneQrInvoice(QrCodeReader.create().readQRCodes(qrImageInput));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(UNEXPECTED_EXCEPTION_DURING_PARSE_MSG, e);
        }
    }    
    
    public List<QrInvoice> scanMultiple(final byte[] qrImageInput) throws ParseException {
        try {
            return mapToQrInvoice(QrCodeReader.create().readQRCodes(qrImageInput));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(UNEXPECTED_EXCEPTION_DURING_PARSE_MSG, e);
        }
    }

    public QrInvoice scanAndValidate(final byte[] qrImageInput) throws ParseException, ValidationException {
        return QrInvoiceCodeParser.create().validate(scan(qrImageInput));
    }

    public QrInvoice scan(final InputStream qrImageInput) throws ParseException {
        try {
            return extractOneQrInvoice(QrCodeReader.create().readQRCodes(qrImageInput));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(UNEXPECTED_EXCEPTION_DURING_PARSE_MSG, e);
        }
    }
    
    public List<QrInvoice> scanMultiple(final InputStream qrImageInput) throws ParseException {
        try {
            return mapToQrInvoice(QrCodeReader.create().readQRCodes(qrImageInput));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(UNEXPECTED_EXCEPTION_DURING_PARSE_MSG, e);
        }
    }

    public QrInvoice scanAndValidate(final InputStream qrImageInput) throws ParseException, ValidationException {
        return QrInvoiceCodeParser.create().validate(scan(qrImageInput));
    }

    public QrInvoice scan(final BufferedImage qrImageInput) throws ParseException {
        try {
            return extractOneQrInvoice(QrCodeReader.create().readQRCodes(qrImageInput));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(UNEXPECTED_EXCEPTION_DURING_PARSE_MSG, e);
        }
    }
    
    public List<QrInvoice> scanMultiple(final BufferedImage qrImageInput) throws ParseException {
        try {
            return mapToQrInvoice(QrCodeReader.create().readQRCodes(qrImageInput));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(UNEXPECTED_EXCEPTION_DURING_PARSE_MSG, e);
        }
    }

    private QrInvoice extractOneQrInvoice(final List<String> qrCodeStrings) {
        final List<String> spcs = SwissQrCodeFilter.filterSwissPaymentCodes(qrCodeStrings);
        if (spcs.isEmpty()) {
            throw new ParseException("No Swiss QR-Code could be found");
        }
        if (spcs.size() > 1) {
            throw new ParseException("Expected exactly one Swiss QR-Code but found " + spcs.size());
        }
        return QrInvoiceCodeParser.create().parse(spcs.get(0));
    }

    private List<QrInvoice> mapToQrInvoice(final List<String> qrCodeStrings) {
        final List<String> spcs = SwissQrCodeFilter.filterSwissPaymentCodes(qrCodeStrings);
        return spcs.stream().map(spc -> QrInvoiceCodeParser.create().parse(spc)).collect(Collectors.toList());
    }

    public QrInvoice scanAndValidate(final BufferedImage qrImageInput) throws ParseException, ValidationException {
        return QrInvoiceCodeParser.create().validate(scan(qrImageInput));
    }


}
