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
package ch.codeblock.qrinvoice.infrastructure;

import ch.codeblock.qrinvoice.paymentpartreceipt.IPaymentPartReceiptWriter;
import ch.codeblock.qrinvoice.paymentpartreceipt.Java2dPaymentPartReceiptWriter;
import ch.codeblock.qrinvoice.paymentpartreceipt.PaymentPartReceiptWriterOptions;
import ch.codeblock.qrinvoice.qrcode.IQrCodeWriter;
import ch.codeblock.qrinvoice.qrcode.JavaGraphicsQrCodeWriter;
import ch.codeblock.qrinvoice.qrcode.QrCodeWriterOptions;

public class JavaGraphicsOutputWriterFactory implements IOutputWriterFactory {

    @Override
    public boolean supports(final Object options) {
        if (options instanceof PaymentPartReceiptWriterOptions) {
            return supports((PaymentPartReceiptWriterOptions) options);
        } else if (options instanceof QrCodeWriterOptions) {
            return supports((QrCodeWriterOptions) options);
        } else {
            return false;
        }
    }

    @Override
    public boolean supports(final QrCodeWriterOptions options) {
        switch (options.getOutputFormat()) {
            case PNG:
            case GIF:
            case TIFF:
            case JPEG:
            case BMP:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean supports(final PaymentPartReceiptWriterOptions options) {
        switch (options.getOutputFormat()) {
            case PNG:
            case GIF:
            case TIFF:
            case JPEG:
            case BMP:
                return true;
            default:
                return false;
        }
    }

    @Override
    public IPaymentPartReceiptWriter create(final PaymentPartReceiptWriterOptions options) {
        return new Java2dPaymentPartReceiptWriter(options);
    }


    @Override
    public IQrCodeWriter create(final QrCodeWriterOptions options) {
        return new JavaGraphicsQrCodeWriter(options);
    }

    @Override
    public String getShortName() {
        return getClass().getSimpleName().replace("OutputWriterFactory", "");
    }

}