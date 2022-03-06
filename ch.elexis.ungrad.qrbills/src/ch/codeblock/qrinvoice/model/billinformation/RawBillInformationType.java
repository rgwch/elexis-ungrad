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

import java.util.EnumSet;
import java.util.Set;

public class RawBillInformationType implements BillInformationType {
    private static final long serialVersionUID = 6868815200647217203L;
    private static Set<BillInformationTypes> nonGeneralTypes;

    private static final RawBillInformationType INSTANCE = new RawBillInformationType();

    private RawBillInformationType() {
    }

    public static RawBillInformationType getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean supports(final String billInformation) {
        return getNonGeneralTypes().stream().noneMatch(type -> type.getBillInformationType().supports(billInformation));
    }

    private Set<BillInformationTypes> getNonGeneralTypes() {
        if (nonGeneralTypes == null) {
            nonGeneralTypes = EnumSet.complementOf(EnumSet.of(BillInformationTypes.GENERAL));
        }
        return nonGeneralTypes;
    }

    @Override
    public RawBillInformation parse(final String billInformation) {
        return new RawBillInformation(billInformation);
    }
}
