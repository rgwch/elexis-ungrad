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
package ch.codeblock.qrinvoice.paymentpartreceipt;

import ch.codeblock.qrinvoice.FontFamily;
import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.OutputResolution;
import ch.codeblock.qrinvoice.PageSize;

import java.util.Locale;

public class PaymentPartReceiptWriterOptions {
    private PageSize pageSize;
    private OutputFormat outputFormat;
    private OutputResolution outputResolution;
    private Locale locale;
    private BoundaryLines boundaryLines;
    private boolean boundaryLineScissors;
    private boolean boundaryLineSeparationText;
    private PaymentPartReceiptLayout layout;
    private FontFamily fontFamily;
    private boolean fontsEmbedded = true;
    private boolean additionalPrintMargin;

    public PageSize getPageSize() {
        return pageSize;
    }

    public void setPageSize(final PageSize pageSize) {
        this.pageSize = pageSize;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(final OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public OutputResolution getOutputResolution() {
        return outputResolution;
    }

    public void setOutputResolution(final OutputResolution outputResolution) {
        this.outputResolution = outputResolution;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public BoundaryLines getBoundaryLines() {
        return boundaryLines;
    }

    public void setBoundaryLines(final BoundaryLines boundaryLines) {
        this.boundaryLines = boundaryLines;
    }

    public boolean isBoundaryLineScissors() {
        return boundaryLineScissors;
    }

    public void setBoundaryLineScissors(final boolean boundaryLineScissors) {
        this.boundaryLineScissors = boundaryLineScissors;
    }

    public boolean isBoundaryLineSeparationText() {
        return boundaryLineSeparationText;
    }

    public void setBoundaryLineSeparationText(final boolean boundaryLineSeparationText) {
        this.boundaryLineSeparationText = boundaryLineSeparationText;
    }

    public void setLayout(final PaymentPartReceiptLayout layout) {
        this.layout = layout;
    }

    public PaymentPartReceiptLayout getLayout() {
        return layout;
    }

    public void setFontFamily(FontFamily fontFamily) {
        this.fontFamily = fontFamily;
    }

    public FontFamily getFontFamily() {
        return fontFamily;
    }

    public boolean isAdditionalPrintMargin() {
        return additionalPrintMargin;
    }

    public void setAdditionalPrintMargin(final boolean additionalPrintMargin) {
        this.additionalPrintMargin = additionalPrintMargin;
    }

    public boolean isFontsEmbedded() {
        return fontsEmbedded;
    }

    public void setFontsEmbedded(final boolean fontsEmbedded) {
        this.fontsEmbedded = fontsEmbedded;
    }
}
