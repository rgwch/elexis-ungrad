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
package ch.codeblock.qrinvoice.util;

import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.model.ReferenceType;

public class ReferenceUtils {
    private ReferenceUtils() {
    }

    public static String format(ReferenceType referenceType, String reference) {
        if (referenceType == null || reference == null) {
            return "";
        }

        switch (referenceType) {
            case QR_REFERENCE:
                return QRReferenceUtils.formatQrReference(reference);
            case CREDITOR_REFERENCE:
                return CreditorReferenceUtils.formatCreditorReference(reference);
            case WITHOUT_REFERENCE:
                if (StringUtils.isNotEmpty(reference)) {
                    throw new TechnicalException("Under no circumstances a reference must be given for reference type NON");
                }
                return "";
            default:
                throw new TechnicalException("Unknown reference type: " + referenceType);
        }
    }

    public static String normalize(ReferenceType referenceType, String reference) {
        if (referenceType == null) {
            return reference;
        }

        switch (referenceType) {
            case QR_REFERENCE:
                return QRReferenceUtils.normalizeQrReference(reference);
            case CREDITOR_REFERENCE:
                return CreditorReferenceUtils.normalizeCreditorReference(reference);
            case WITHOUT_REFERENCE:
            default:
                return reference;
        }
    }
}
