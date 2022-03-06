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

import ch.codeblock.qrinvoice.config.SystemProperties;
import ch.codeblock.qrinvoice.layout.Dimension;
import ch.codeblock.qrinvoice.qrcode.SwissQrCode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Locale.*;

/**
 * Dimensions are in millimeters, font-sizes in points.
 */
public class LayoutDefinitions {

    private LayoutDefinitions() {
    }

    /**
     * Decimal separator according to the spec 3.5.3 - Amount section - v2.0
     */
    public static final char AMOUNT_DECIMAL_FORMAT_DECIMAL_SEPARATOR = '.';

    /**
     * Thousands separator according to the spec 3.5.3 - Amount section - v2.0
     */
    public static final char AMOUNT_DECIMAL_FORMAT_GROUPING_SEPARATOR = ' ';

    /**
     * Format according to the spec 3.5.3 - Amount section - v2.0
     */
    public static final String AMOUNT_DECIMAL_FORMAT_PRINT = "#,###,###.##";

    public static final Set<Locale> SUPPORTED_LOCALES = Collections.unmodifiableSet(new HashSet<>(asList(GERMAN, FRENCH, ENGLISH, ITALIAN)));
    public static final Locale DEFAULT_LOCALE = GERMAN;

    // ----------------------------------------------------------------------------------------------------------
    // Font sizes
    // ----------------------------------------------------------------------------------------------------------
    /**
     * Fixed font size (in bold) for the title section of both receipt and payment part according to the spec 3.5.1 and 3.6.1 - v2.0
     */
    static final int FONT_SIZE_TITLE = 11;
    /**
     * Fixed font size for the separation label obove the payment part - size not specified in the spec v2.0
     */
    static final int FONT_SIZE_SEPARATION_LABEL = 7;
    /**
     * Smallest possible font size according to the spec 3.4 - Fonts and font sizes - v2.0
     */
    private static final int FONT_SIZE_MIN = 6;
    /**
     * Smallest possible font size for values according to the spec 3.4 - Fonts and font sizes - v2.0 (smallest font size is 6 and heading must be 2pt smaller than value -&gt; 6 + 2 = 8)
     */
    private static final int FONT_SIZE_DIFFERENCE_VALUES_HEADING = 2;

    static final int RECEIPT_FONT_SIZE_HEADING_MIN = FONT_SIZE_MIN;
    static final int RECEIPT_FONT_SIZE_VALUE_MIN = RECEIPT_FONT_SIZE_HEADING_MIN + FONT_SIZE_DIFFERENCE_VALUES_HEADING;

    static final int PAYMENT_PART_FONT_SIZE_HEADING_RECOMMENDED = 8;
    static final int PAYMENT_PART_FONT_SIZE_VALUE_RECOMMENDED = PAYMENT_PART_FONT_SIZE_HEADING_RECOMMENDED + FONT_SIZE_DIFFERENCE_VALUES_HEADING;

    /**
     * Fixed font size for the further information section 3.5.5 - v2.0
     */
    static final int PAYMENT_PART_FONT_SIZE_FURTHER_INFO = 7;

    // ----------------------------------------------------------------------------------------------------------
    // Paragraph & line spacing
    // ----------------------------------------------------------------------------------------------------------

    /**
     * Line spacing is 9 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 9 - 6 (font-size) as leading 3
     */
    static final int RECEIPT_HEADING_LINE_SPACING_PTS = 3;
    /**
     * Line spacing is 9 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 9 - 8 (font-size) as leading 1
     */
    static final int RECEIPT_VALUE_LINE_SPACING_PTS = 1;
    /**
     * Line spacing is 9 according to the qr bill style guide - page 15 (version 16.01.2019)
     */
    static final int RECEIPT_PARAGRAPH_SPACING_PTS = 9;
    /**
     * Line spacing is 11 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 11 - 8 (font-size) as leading 3
     */
    static final int RECEIPT_AMOUNT_LINE_SPACING_PTS = 3;

    /**
     * Line spacing is 11 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 11 - 8 (font-size) as leading 3
     */
    static final int PAYMENT_PART_HEADING_LINE_SPACING_PTS = 3;
    /**
     * Line spacing is 11 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 11 - 10 (font-size) as leading 1
     */
    static final int PAYMENT_PART_VALUE_LINE_SPACING_PTS = 1;
    /**
     * Line spacing is 11 according to the qr bill style guide - page 15 (version 16.01.2019)
     */
    static final int PAYMENT_PART_PARAGRAPH_SPACING_PTS = 11;
    /**
     * Line spacing is 13 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 13 - 10 (font-size) as leading 3
     */
    static final int PAYMENT_PART_AMOUNT_LINE_SPACING_PTS = 3;
    /**
     * Line spacing is 8 according to the qr bill style guide - page 15 (version 16.01.2019) - we take 8 - 7 (font-size) as leading 3
     */
    static final int PAYMENT_PART_FURTHER_INFO_LINE_SPACING_PTS = 1;

    // ----------------------------------------------------------------------------------------------------------
    // Layout
    // ----------------------------------------------------------------------------------------------------------
    /**
     * Min 5mm quiet space according to the spec 3.5 - Sections of the payment part - v2.0
     */
    static final int QUIET_SPACE = 5;
    /**
     * Basically we fully adhere to the style guide, which state 5mm (quiet space) as a print margin.
     * However tests with printers and print services have shown that 5mm might just not be enough, adding an extra mm solves most of these issues.
     */
    static final int ADDITIONAL_PRINT_MARGIN = (System.getProperty(SystemProperties.DISABLE_ADDITIONAL_PRINT_MARGIN) != null) ? 0 : 1;
    private static final int QUIET_SPACE_DOUBLED = QUIET_SPACE * 2;

    static final float BOX_CORNER_LINE_LENGTH = 3f;
    /**
     * line thickness 0.75 pt according to the spec v2.0 (equals ~0.2646mm)
     */
    static final float BOX_CORNER_LINE_WIDTH = 0.2646f;

    static final int SCISSOR_LENGTH = 5;

    static final Dimension<Float> RECEIPT = new Dimension<>(62f, 105f);
    static final Dimension<Float> PAYMENT_PART = new Dimension<>(148f, 105f);
    static final Dimension<Float> RECEIPT_PAYMENT_PART = new Dimension<>(210f, 105f);

    static final int APPROX_MAX_LINE_LENGTH_PAYMENT_PART_INFO_SECTION = 40;

    // ----------------------------------------------------------------------------------------------------------
    // Receipt
    // ----------------------------------------------------------------------------------------------------------

    /**
     * Size according to the spec 3.6.2 - Information section - v2.0
     */
    static final Dimension<Float> RECEIPT_DEBTOR_FIELD = new Dimension<>(52f, 20f);
    /**
     * Size according to the spec 3.6.3 - Amount section - v2.0
     */
    static final Dimension<Float> RECEIPT_AMOUNT_FIELD = new Dimension<>(30f, 10f);
    /**
     * Height is 14mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> RECEIPT_AMOUNT_SECTION = new Dimension<>(RECEIPT.getWidth() - QUIET_SPACE_DOUBLED, 14f);
    /**
     * Size of the acceptance section is not explicitly specified in the spec 3.6.4 - Acceptance point section - v2.0<br>
     * Width is obviously the width of the receipt - two times the quiet space.<br>
     * Height is 18mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> RECEIPT_ACCEPTANCE_SECTION = new Dimension<>(RECEIPT.getWidth() - QUIET_SPACE_DOUBLED, 18f);
    /**
     * Size of the title section is not explicitly specified in the spec 3.6.1 - Title section - v2.0<br>
     * Width is obviously the width of the receipt - two times the quiet space.<br>
     * Height is 7mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> RECEIPT_TITLE_SECTION = new Dimension<>(RECEIPT.getWidth() - QUIET_SPACE_DOUBLED, 7f);
    static final Dimension<Float> RECEIPT_INFORMATION_SECTION = new Dimension<>(RECEIPT.getWidth() - QUIET_SPACE_DOUBLED,
            RECEIPT.getHeight() //
                    - QUIET_SPACE_DOUBLED
                    - RECEIPT_TITLE_SECTION.getHeight() //
                    - RECEIPT_AMOUNT_SECTION.getHeight() //
                    - RECEIPT_ACCEPTANCE_SECTION.getHeight());

    // ----------------------------------------------------------------------------------------------------------
    // PaymentPart
    // ----------------------------------------------------------------------------------------------------------

    /**
     * Size according to the spec 3.5.4 - Information section - v2.0
     */
    static final Dimension<Float> PAYMENT_PART_DEBTOR_FIELD = new Dimension<>(65f, 25f);
    /**
     * Size according to the spec 3.5.3 - Amount section - v2.0
     */
    static final Dimension<Float> PAYMENT_PART_AMOUNT_FIELD = new Dimension<>(40f, 15f);

    /**
     * Left column in the payment part consist of the title, the swiss qr code and the amount section.<br>
     * Due to the swiss qr code and the quiet space, we need the column to be at least the size of the qr_code (46mm) plus the quiet space (5mm).<br>
     * Width is 51mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    private static final Float PAYMENT_PART_LEFT_COL = SwissQrCode.QR_CODE_SIZE.getWidth() + QUIET_SPACE;
    /**
     * In the right column of the payment part, there is only the information section.<br>
     * It takes the space that is left from the payment part minus left column minus quiet space on both sides of the payment part.<br>
     * Width is 87mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    private static final Float PAYMENT_PART_RIGHT_COL = PAYMENT_PART.getWidth() - QUIET_SPACE_DOUBLED - PAYMENT_PART_LEFT_COL;
    /**
     * Size of the title section is not explicitly specified in the spec 3.5.1 - Title section - v2.0<br>
     * Width is about the qr code + 2 times the quiet space we added as distance from the qr code to the information section<br>
     * Height is 7mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> PAYMENT_PART_TITLE_SECTION = new Dimension<>(PAYMENT_PART_LEFT_COL, 7f);
    /**
     * Height is 22mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> PAYMENT_PART_AMOUNT_SECTION = new Dimension<>(PAYMENT_PART_LEFT_COL, 22f);
    /**
     * Height is 56mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> PAYMENT_PART_QR_SECTION = new Dimension<>(PAYMENT_PART_LEFT_COL, SwissQrCode.QR_CODE_SIZE.getWidth() + QUIET_SPACE_DOUBLED);

    /**
     * Size of the further information section is not explicitly specified in the spec 3.5.5 - Title section - v2.0<br>
     * Width is the payment part width minus 2 times the quiet space (left and right)<br>
     * Height is 10mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> PAYMENT_PART_FURTHER_INFO_SECTION = new Dimension<>(PAYMENT_PART.getWidth() - QUIET_SPACE_DOUBLED, 10f);

    /**
     * Width is the payment part width minus 2 times the quiet space (left and right) minus swiss qr code section<br>
     * Height is the payment part height minus 2 times the quiet space minus the info section, or 85mm according to the qr bill style guide - page 6 (version 16.01.2019)
     */
    static final Dimension<Float> PAYMENT_PART_INFORMATION_SECTION = new Dimension<>(PAYMENT_PART_RIGHT_COL,
            PAYMENT_PART.getHeight() //
                    - QUIET_SPACE_DOUBLED
                    - PAYMENT_PART_FURTHER_INFO_SECTION.getHeight()
    );

    static final String MORE_TEXT_INDICATOR = "...";

}
