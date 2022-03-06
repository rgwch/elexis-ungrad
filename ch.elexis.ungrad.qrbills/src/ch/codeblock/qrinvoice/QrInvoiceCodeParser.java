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

import ch.codeblock.qrinvoice.model.AdditionalInformation;
import ch.codeblock.qrinvoice.model.ParseException;
import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.model.billinformation.BillInformation;
import ch.codeblock.qrinvoice.model.mapper.SwissPaymentsCodeToModelMapper;
import ch.codeblock.qrinvoice.model.parser.BillInformationParser;
import ch.codeblock.qrinvoice.model.parser.SwissPaymentsCodeParser;
import ch.codeblock.qrinvoice.model.validation.QrInvoiceValidator;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.model.validation.ValidationOptions;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QrInvoiceCodeParser {

    public static QrInvoiceCodeParser create() {
        return new QrInvoiceCodeParser();
    }

    private final Logger logger = LoggerFactory.getLogger(QrInvoiceCodeParser.class);

    public QrInvoice parse(final String swissPaymentsCode) throws ParseException {
        return parse(swissPaymentsCode, false);
    }

    public QrInvoice parse(final String swissPaymentsCode, final boolean parseBillInformation) throws ParseException {
        try {
            final SwissPaymentsCode spc = SwissPaymentsCodeParser.create().parse(swissPaymentsCode);
            final QrInvoice qrInvoice = SwissPaymentsCodeToModelMapper.create().map(spc);

            if (parseBillInformation) {
                applyBillInformationObject(qrInvoice);
            }

            return qrInvoice;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException("Unexpected exception occurred during parsing of a Swiss Payment Code as QrInvoice", e);
        }
    }

    public void applyBillInformationObject(final QrInvoice qrInvoice) {
        if (qrInvoice != null && qrInvoice.getPaymentReference() != null) {
            final AdditionalInformation additionalInformation = qrInvoice.getPaymentReference().getAdditionalInformation();
            if (additionalInformation != null && StringUtils.isNotEmpty(additionalInformation.getBillInformation())) {
                final String billInformation = additionalInformation.getBillInformation();
                final BillInformation billInformationObject = BillInformationParser.create().parseBillInformation(billInformation);
                additionalInformation.setBillInformationObject(billInformationObject);
            }
        }
    }

    public QrInvoice parseAndValidate(final String swissPaymentsCode, final boolean parseBillInformation) throws ParseException, ValidationException {
        return validate(parse(swissPaymentsCode, parseBillInformation));
    }

    public QrInvoice parseAndValidate(final String swissPaymentsCode) throws ParseException, ValidationException {
        return parseAndValidate(swissPaymentsCode, false);
    }

    protected QrInvoice validate(final QrInvoice qrInvoice) {
        final ValidationOptions validationOptions = new ValidationOptions();
        validationOptions.setSkipBillInformationObject(true); // because billInformationObject representation does not matter onr read
        QrInvoiceValidator.create().validate(qrInvoice, validationOptions).throwExceptionOnErrors();
        return qrInvoice;
    }

}
