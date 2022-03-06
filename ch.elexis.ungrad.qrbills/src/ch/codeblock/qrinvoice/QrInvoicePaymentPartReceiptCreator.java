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
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.output.PaymentPartReceipt;
import ch.codeblock.qrinvoice.paymentpartreceipt.*;

import java.util.Locale;

public class QrInvoicePaymentPartReceiptCreator {
    private QrInvoice qrInvoice;
    private PageSize pageSize;
    private FontFamily fontFamily;
    private OutputFormat outputFormat;
    private OutputResolution outputResolution;
    private Locale locale;
    private BoundaryLines boundaryLines;
    private Boolean boundaryLineScissors;
    private Boolean boundaryLineSeparationText;
    private Boolean additionalPrintMargin;
    private Boolean fontsEmbedded;

    protected QrInvoicePaymentPartReceiptCreator() {
    }

    public static QrInvoicePaymentPartReceiptCreator create() {
        return new QrInvoicePaymentPartReceiptCreator();
    }

    public QrInvoicePaymentPartReceiptCreator qrInvoice(final QrInvoice qrInvoice) {
        this.qrInvoice = qrInvoice;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator outputFormat(final OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator outputResolution(final OutputResolution outputResolution) {
        this.outputResolution = outputResolution;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator pageSize(final PageSize pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator fontFamily(final FontFamily fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator locale(final Locale locale) {
        if (locale == null) {
            return this;
        }
        if (!LayoutDefinitions.SUPPORTED_LOCALES.contains(locale)) {
            throw new ValidationException("Unsupported locale '" + locale + "'");
        }
        this.locale = locale;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLines(final Boolean boundaryLines) {
        if (boundaryLines != null) {
            return boundaryLines(boundaryLines.booleanValue());
        }
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLines(final boolean boundaryLines) {
        this.boundaryLines = boundaryLines ? BoundaryLines.ENABLED : BoundaryLines.NONE;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLines(final BoundaryLines boundaryLines) {
        this.boundaryLines = boundaryLines;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator withBoundaryLines() {
        return boundaryLines(true);
    }

    public QrInvoicePaymentPartReceiptCreator withBoundaryLinesWithMargins() {
        return boundaryLines(BoundaryLines.ENABLED_WITH_MARGINS);
    }

    public QrInvoicePaymentPartReceiptCreator withoutBoundaryLines() {
        return boundaryLines(false);
    }

    public QrInvoicePaymentPartReceiptCreator withScissors() {
        return boundaryLineScissors(true);
    }

    public QrInvoicePaymentPartReceiptCreator withoutScissors() {
        return boundaryLineScissors(false);
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLineScissors(final Boolean boundaryLineScissors) {
        if (boundaryLineScissors != null) {
            return boundaryLineScissors(boundaryLineScissors.booleanValue());
        }
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLineScissors(final boolean boundaryLineScissors) {
        this.boundaryLineScissors = boundaryLineScissors;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator withSeparationText() {
        return boundaryLineSeparationText(true);
    }

    public QrInvoicePaymentPartReceiptCreator withoutSeparationText() {
        return boundaryLineSeparationText(false);
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLineSeparationText(final Boolean boundaryLineSeparationText) {
        if (boundaryLineSeparationText != null) {
            return boundaryLineSeparationText(boundaryLineSeparationText.booleanValue());
        }
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator boundaryLineSeparationText(final boolean boundaryLineSeparationText) {
        this.boundaryLineSeparationText = boundaryLineSeparationText;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator additionalPrintMargin(final Boolean additionalPrintMargin) {
        if (additionalPrintMargin != null) {
            return additionalPrintMargin(additionalPrintMargin.booleanValue());
        }
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator additionalPrintMargin(final boolean additionalPrintMargin) {
        this.additionalPrintMargin = additionalPrintMargin;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator withAdditionalPrintMargin() {
        return additionalPrintMargin(true);
    }

    public QrInvoicePaymentPartReceiptCreator withoutAdditionalPrintMargin() {
        return additionalPrintMargin(false);
    }

    public QrInvoicePaymentPartReceiptCreator inGerman() {
        this.locale(Locale.GERMAN);
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator inFrench() {
        this.locale(Locale.FRENCH);
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator inEnglish() {
        this.locale(Locale.ENGLISH);
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator inItalian() {
        this.locale(Locale.ITALIAN);
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator fontsEmbedded(final Boolean fontsEmbedded) {
        if (fontsEmbedded != null) {
            return fontsEmbedded(fontsEmbedded.booleanValue());
        }
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator fontsEmbedded(final boolean fontsEmbedded) {
        this.fontsEmbedded = fontsEmbedded;
        return this;
    }

    public QrInvoicePaymentPartReceiptCreator disableFontEmbedding() {
        return fontsEmbedded(false);
    }

    public QrInvoicePaymentPartReceiptCreator enableFontEmbedding() {
        return fontsEmbedded(true);
    }

    public PaymentPartReceipt createPaymentPartReceipt() {
        applyDefaults();
        validateOptions();

        final PaymentPartReceiptWriterOptions paymentPartReceiptWriterOptions = new PaymentPartReceiptWriterOptions();
        paymentPartReceiptWriterOptions.setPageSize(pageSize);
        paymentPartReceiptWriterOptions.setOutputFormat(outputFormat);
        paymentPartReceiptWriterOptions.setFontFamily(fontFamily);
        paymentPartReceiptWriterOptions.setOutputResolution(outputResolution);
        paymentPartReceiptWriterOptions.setLocale(locale);
        paymentPartReceiptWriterOptions.setBoundaryLines(boundaryLines);
        paymentPartReceiptWriterOptions.setBoundaryLineScissors(boundaryLineScissors);
        paymentPartReceiptWriterOptions.setBoundaryLineSeparationText(boundaryLineSeparationText);
        paymentPartReceiptWriterOptions.setAdditionalPrintMargin(additionalPrintMargin);
        paymentPartReceiptWriterOptions.setFontsEmbedded(fontsEmbedded);

        paymentPartReceiptWriterOptions.setLayout(PaymentPartReceiptLayoutHelper.create().chooseLayout(qrInvoice));

        return PaymentPartReceiptWriter.create().write(paymentPartReceiptWriterOptions, qrInvoice);
    }

    QrInvoicePaymentPartReceiptCreator applyDefaults() {
        // apply defaults, if not explicitly set
        if (pageSize == null) {
            pageSize = PageSize.A4;
        }
        if (outputFormat == null) {
            outputFormat = OutputFormat.PDF;
        }
        if (outputResolution == null) {
            outputResolution = OutputResolution.MEDIUM_300_DPI;
        }
        if (locale == null) {
            locale = LayoutDefinitions.DEFAULT_LOCALE;
        }
        if (boundaryLines == null) {
            boundaryLines = BoundaryLines.ENABLED;
        }
        if (boundaryLineSeparationText == null) {
            boundaryLineSeparationText = Boolean.FALSE;
        }
        // if boundaryLineSeparationText is not used, apply scissors as default if boundary line is chosen
        if (boundaryLineScissors == null) {
            boundaryLineScissors = (boundaryLines != BoundaryLines.NONE) && !boundaryLineSeparationText;
        }
        if (additionalPrintMargin == null) {
            additionalPrintMargin = Boolean.FALSE;
        }
        if (fontFamily == null) {
            fontFamily = FontFamily.LIBERATION_SANS;
        }
        if (fontsEmbedded == null) {
            fontsEmbedded = Boolean.TRUE;
        }
        return this;
    }

    QrInvoicePaymentPartReceiptCreator validateOptions() {
        if (boundaryLines == BoundaryLines.NONE && boundaryLineScissors) {
            throw new ValidationException("BoundaryLineScissors is only to be used with boundaryLines option for non-perforated output");
        }
        if (boundaryLineSeparationText && pageSize.smallerThan(PageSize.A5)) {
            throw new ValidationException("BoundaryLineSeparationText is only to be used with PageSize A4 or A5. Text is printed above payment part and needs more space than DIN-Lang");
        }
        return this;
    }

}
