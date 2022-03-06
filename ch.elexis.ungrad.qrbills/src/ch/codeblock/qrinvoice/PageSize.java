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

public enum PageSize {
    /** A4 portrait (210 x 297 mm) */
    A4(3),
    /** A5 landscape (210 x 148 mm) */
    A5(2),
    /** DIN_LANG landscape (210 x 105 mm) */
    DIN_LANG(1),
    /** DIN_LANG_CROPPED landscape (200 x 100 mm) OR (198 x 99 mm) in case "additional print margin" (6mm instead of 5mm) is requested */
    DIN_LANG_CROPPED(0)
    ;

    private final int sizeRank;

    PageSize(final int sizeRank) {
        this.sizeRank = sizeRank;
    }

    public boolean greaterThan(PageSize pageSize) {
        return this.sizeRank > pageSize.sizeRank;
    }

    public boolean smallerThan(PageSize pageSize) {
        return this.sizeRank < pageSize.sizeRank;
    }

    public boolean sameAs(PageSize pageSize) {
        return this == pageSize;
    }
    
    public boolean differentFrom(PageSize pageSize) {
        return this != pageSize;
    }
}
