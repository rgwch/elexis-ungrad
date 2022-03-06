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

import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.mapper.ModelToSwissPaymentsCodeMapper;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.output.QrCode;
import ch.codeblock.qrinvoice.qrcode.QrCodeWriter;
import ch.codeblock.qrinvoice.qrcode.QrCodeWriterOptions;

public class QrInvoiceCodeCreator {
    private QrInvoice qrInvoice;
    private int desiredQrCodeSize = QrCodeWriterOptions.DESIRED_QR_CODE_SIZE_UNSET;
    private OutputFormat outputFormat;
    private OutputResolution outputResolution;

    private QrInvoiceCodeCreator() {

    }

    public static QrInvoiceCodeCreator create() {
        return new QrInvoiceCodeCreator();
    }

    public String createSwissPaymentsCode() {
        return ModelToSwissPaymentsCodeMapper.create().map(qrInvoice).toSwissPaymentsCodeString();
    }

    public QrInvoiceCodeCreator qrInvoice(final QrInvoice qrInvoice) {
        this.qrInvoice = qrInvoice;
        return this;
    }

    public QrInvoiceCodeCreator outputFormat(final OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public QrInvoiceCodeCreator outputResolution(final OutputResolution outputResolution) {
        this.outputResolution = outputResolution;
        return this;
    }

    /***
     *
     * @param desiredQrCodeSize in pixels
     * @return The {@link QrInvoiceCodeCreator} instance
     */
    public QrInvoiceCodeCreator desiredQrCodeSize(final int desiredQrCodeSize) {
        this.desiredQrCodeSize = desiredQrCodeSize;
        return this;
    }

    public QrCode createQrCode() {
        if (desiredQrCodeSize > QrCodeWriterOptions.DESIRED_QR_CODE_SIZE_UNSET && outputResolution != null) {
            throw new ValidationException("Either specify desiredQrCodeSize OR outputResolution");
        }

        if (outputFormat == OutputFormat.PDF && desiredQrCodeSize > QrCodeWriterOptions.DESIRED_QR_CODE_SIZE_UNSET) {
            throw new ValidationException("desiredQrCodeSize cannot be used with output format PDF");
        }

        // apply defaults, if not explicitly set
        if (outputFormat == null) {
            outputFormat = OutputFormat.PNG;
        }

        if (outputResolution == null && desiredQrCodeSize == QrCodeWriterOptions.DESIRED_QR_CODE_SIZE_UNSET) {
            outputResolution = OutputResolution.MEDIUM_300_DPI;
        }

        final QrCodeWriterOptions qrCodeWriterOptions = new QrCodeWriterOptions();
        qrCodeWriterOptions.setOutputFormat(outputFormat);
        qrCodeWriterOptions.setDesiredQrCodeSize(desiredQrCodeSize);
        qrCodeWriterOptions.setOutputResolution(outputResolution);

        return QrCodeWriter.create().write(qrCodeWriterOptions, createSwissPaymentsCode());
    }

}
