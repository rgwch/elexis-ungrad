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
package ch.codeblock.qrinvoice.model.billinformation;

import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.annotation.Description;
import ch.codeblock.qrinvoice.model.validation.ValidationResult;

@Description("Interface type, see SwicoS1v12 or RawBillInformation")
public interface BillInformation {
    String getBillInformationType();

    ValidationResult validate();

    ValidationResult validate(QrInvoice qrInvoice);

    void validate(ValidationResult result, QrInvoice qrInvoice);

    void validate(ValidationResult result);

    String toBillInformationString();
}
