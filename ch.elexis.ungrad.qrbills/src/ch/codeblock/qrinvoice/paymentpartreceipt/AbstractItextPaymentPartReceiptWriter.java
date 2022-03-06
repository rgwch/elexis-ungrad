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
import ch.codeblock.qrinvoice.fonts.FontManager;
import ch.codeblock.qrinvoice.fonts.FontStyle;
import ch.codeblock.qrinvoice.layout.Dimension;
import ch.codeblock.qrinvoice.layout.DimensionUnitUtils;
import ch.codeblock.qrinvoice.layout.Rect;
import ch.codeblock.qrinvoice.qrcode.SwissQrCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.codeblock.qrinvoice.layout.DimensionUnitUtils.millimetersToPoints;

public abstract class AbstractItextPaymentPartReceiptWriter implements IPaymentPartReceiptWriter {
    static final String PDF_ENCODING = StandardCharsets.ISO_8859_1.name();

    // -----
    // Fonts
    // -----
    static final int FONT_SIZE_TITLE = LayoutDefinitions.FONT_SIZE_TITLE;
    static final int PAYMENT_PART_FONT_SIZE_HEADING = LayoutDefinitions.PAYMENT_PART_FONT_SIZE_HEADING_RECOMMENDED;
    static final int PAYMENT_PART_FONT_SIZE_VALUE = LayoutDefinitions.PAYMENT_PART_FONT_SIZE_VALUE_RECOMMENDED;
    static final int PAYMENT_PART_FONT_SIZE_FURTHER_INFO = LayoutDefinitions.PAYMENT_PART_FONT_SIZE_FURTHER_INFO;
    static final int RECEIPT_FONT_SIZE_HEADING = LayoutDefinitions.RECEIPT_FONT_SIZE_HEADING_MIN;
    static final int RECEIPT_FONT_SIZE_VALUE = LayoutDefinitions.RECEIPT_FONT_SIZE_VALUE_MIN;

    // -----
    // Line and paragraph spacings
    // -----
    static final float PAYMENT_PART_FURTHER_INFO_LINE_SPACING = LayoutDefinitions.PAYMENT_PART_FURTHER_INFO_LINE_SPACING_PTS;
    static final float PAYMENT_PART_AMOUNT_LINE_SPACING = LayoutDefinitions.PAYMENT_PART_AMOUNT_LINE_SPACING_PTS;
    static final float PAYMENT_PART_PARAGRAPH_SPACING = LayoutDefinitions.PAYMENT_PART_PARAGRAPH_SPACING_PTS;
    static final float PAYMENT_PART_VALUE_LINE_SPACING = LayoutDefinitions.PAYMENT_PART_VALUE_LINE_SPACING_PTS;
    static final float PAYMENT_PART_HEADING_LINE_SPACING = LayoutDefinitions.PAYMENT_PART_HEADING_LINE_SPACING_PTS;

    static final float RECEIPT_AMOUNT_LINE_SPACING = LayoutDefinitions.RECEIPT_AMOUNT_LINE_SPACING_PTS;
    static final float RECEIPT_PARAGRAPH_SPACING = LayoutDefinitions.RECEIPT_PARAGRAPH_SPACING_PTS;
    static final float RECEIPT_VALUE_LINE_SPACING = LayoutDefinitions.RECEIPT_VALUE_LINE_SPACING_PTS;
    static final float RECEIPT_HEADING_LINE_SPACING = LayoutDefinitions.RECEIPT_HEADING_LINE_SPACING_PTS;

    // multiplied leading is always 1 here - it is multiplied with font size and thus this matched with the additional, fixed leading defined in the layout definitions
    static final float MULTIPLIED_LEADING = 1f;

    //
    static final float BOUNDARY_LINE_WIDTH = 0.5f;

    private static final Rect<Float> A6_LANDSCAPE = Rect.create(0f, 0f, 420f, 297f);
    static final Rect<Float> DIN_LANG = Rect.create(0f, 0f, 595f, 297f);
    private static final Rect<Float> PAYMENT_PART = A6_LANDSCAPE;

    static final float QUIET_SPACE_PTS = millimetersToPoints(LayoutDefinitions.QUIET_SPACE);
    static final float BOX_CORNER_LINE_WIDTH = millimetersToPoints(LayoutDefinitions.BOX_CORNER_LINE_WIDTH);
    static final float BOX_CORNER_LINE_LENGTH = millimetersToPoints(LayoutDefinitions.BOX_CORNER_LINE_LENGTH);
    static final Dimension<Float> PAYMENT_PART_AMOUNT_FIELD = mmToPoints(LayoutDefinitions.PAYMENT_PART_AMOUNT_FIELD);
    static final Dimension<Float> PAYMENT_PART_DEBTOR_FIELD = mmToPoints(LayoutDefinitions.PAYMENT_PART_DEBTOR_FIELD);

    static final Dimension<Float> RECEIPT_AMOUNT_FIELD = mmToPoints(LayoutDefinitions.RECEIPT_AMOUNT_FIELD);
    static final Dimension<Float> RECEIPT_DEBTOR_FIELD = mmToPoints(LayoutDefinitions.RECEIPT_DEBTOR_FIELD);
    public static final String QR_INVOICE_PRODUCER = "QR Invoice Solutions (c) Codeblock GmbH";

    static Dimension<Float> mmToPoints(final Dimension<Float> mmDimension) {
        return new Dimension<>(millimetersToPoints(mmDimension.getWidth()), millimetersToPoints(mmDimension.getHeight()));
    }

    static final Rect<Float> RECEIPT_ACCEPTANCE_SECTION_RECT = LayoutDefinitions.RECEIPT_ACCEPTANCE_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, QUIET_SPACE_PTS);
    static final Rect<Float> RECEIPT_AMOUNT_SECTION_RECT = LayoutDefinitions.RECEIPT_AMOUNT_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, RECEIPT_ACCEPTANCE_SECTION_RECT.getTopY());
    static final Rect<Float> RECEIPT_INFORMATION_SECTION_RECT = LayoutDefinitions.RECEIPT_INFORMATION_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, RECEIPT_AMOUNT_SECTION_RECT.getTopY());
    static final Rect<Float> RECEIPT_TITLE_SECTION_RECT = LayoutDefinitions.RECEIPT_TITLE_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, RECEIPT_INFORMATION_SECTION_RECT.getTopY());

    static final Rect<Float> PAYMENT_PART_FURTHER_INFO_SECTION_RECT = LayoutDefinitions.PAYMENT_PART_FURTHER_INFO_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, QUIET_SPACE_PTS);
    static final Rect<Float> PAYMENT_PART_AMOUNT_SECTION_RECT = LayoutDefinitions.PAYMENT_PART_AMOUNT_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, PAYMENT_PART_FURTHER_INFO_SECTION_RECT.getTopY());
    static final Rect<Float> PAYMENT_PART_QR_SECTION_RECT = LayoutDefinitions.PAYMENT_PART_QR_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, PAYMENT_PART_AMOUNT_SECTION_RECT.getTopY());
    static final Rect<Float> PAYMENT_PART_INFORMATION_SECTION_RECT = LayoutDefinitions.PAYMENT_PART_INFORMATION_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(PAYMENT_PART_AMOUNT_SECTION_RECT.getRightX(), PAYMENT_PART_FURTHER_INFO_SECTION_RECT.getTopY());
    static final Rect<Float> PAYMENT_PART_TITLE_SECTION_RECT = LayoutDefinitions.PAYMENT_PART_TITLE_SECTION.transform(DimensionUnitUtils::millimetersToPoints).toRectangle(QUIET_SPACE_PTS, PAYMENT_PART_QR_SECTION_RECT.getTopY());

    static float getPaymentPartWidth() {
        return PAYMENT_PART.getWidth();
    }

    static float getPaymentPartHeight() {
        return PAYMENT_PART.getHeight();
    }

    private ResourceBundle labels;
    private PaymentPartReceiptWriterOptions options;
    private final Logger logger = LoggerFactory.getLogger(AbstractItextPaymentPartReceiptWriter.class);

    AbstractItextPaymentPartReceiptWriter(final PaymentPartReceiptWriterOptions options) {
        this.labels = ResourceBundle.getBundle("qrinvoice", options.getLocale());
        this.options = options;
    }

    @Override
    public int getQrCodeImageSize() {
        final int dpi = options.getOutputResolution().getDpi();
        final float qrCodeMillimeters = SwissQrCode.QR_CODE_SIZE.getWidth();
        // millimeters to pixels
        return DimensionUnitUtils.millimetersToPointsRounded(qrCodeMillimeters, dpi);
    }

    String getLabel(final String key) {
        return labels.getString(key);
    }

    PaymentPartReceiptWriterOptions getOptions() {
        return options;
    }

    private static final String MATCH_CHAR_PRESERVE = "((?<=%1$s)|(?=%1$s))";
    private static final Pattern NATURAL_LINE_WRAP_CHARS_PATTERN = Pattern.compile(String.format(MATCH_CHAR_PRESERVE, "[- ]"));

    void applyOptimalLineSplitting(final String text, final float informationSectionWidth, final Function<String, Float> widthFunction, final Runnable aggressiveSplitting) {
        if (text.length() < 30) {
            logger.trace("below 30 chars, we do not need to check informationSectionWidth");
            return;
        }

        float textWidth = widthFunction.apply(text);
        final int nrOfLines = (int) Math.ceil(textWidth / informationSectionWidth);

        if (nrOfLines > 2) {
            // there are more than two lines, thus applying aggressive splitting without further checks
            logger.debug("applying aggressive splitting");
            aggressiveSplitting.run();
        } else if (nrOfLines > 1) {
            logger.trace("there is more than one line required for the given payload");

            final Matcher matcher = NATURAL_LINE_WRAP_CHARS_PATTERN.matcher(text);
            // lastIndexLine1 will finally be set to the lastIndex of the given text that could be place on line 1 when using the natural line wrapping (by space or -)
            // it does like the natural "sweet spot" for wrapping this string into two lines 
            int lastIndexLine1 = 0;
            while (matcher.find()) {
                final int matchEndIndex = matcher.end();
                final String part = text.substring(0, matchEndIndex);
                logger.trace("Matched until index={} text={}", matchEndIndex, part);
                final float widthPoint = widthFunction.apply(part);
                if (widthPoint < informationSectionWidth) {
                    lastIndexLine1 = matchEndIndex;
                } else {
                    // we already know the breaking point
                    break;
                }
            }

            // we found the natural line wrapping point for line 1, now check if the remaining string can be place on line2 without further wrapping
            final String line2 = text.substring(lastIndexLine1, text.length());
            final Float requiredWidthLine2 = widthFunction.apply(line2);
            if (requiredWidthLine2 > informationSectionWidth) {
                logger.debug("natural line wrapping is not best, thus applying aggressive splitting");
                aggressiveSplitting.run();
            } // else natural wrapping is fine

            if (logger.isTraceEnabled()) {
                final String line1 = text.substring(0, lastIndexLine1);
                final Float requiredWidthLine1 = widthFunction.apply(line1);
                logger.trace("Line 1 width={} text={}", requiredWidthLine1, line1);
                logger.trace("Line 2 width={} text={}", requiredWidthLine2, line2);
            }
        }
        // else: no splitting needed
    }

    /**
     * @param fontFamily The font family
     * @param fontStyle  The font style
     * @return Either a font name or a path to a TTF font
     */
    String getFontNameOrFontPath(final FontFamily fontFamily, final FontStyle fontStyle) {
        if (fontFamily == FontFamily.HELVETICA && !options.isFontsEmbedded()) {
            // https://en.wikipedia.org/wiki/Portable_Document_Format#Standard_Type_1_Fonts_(Standard_14_Fonts)
            // Helvetica is one of the standard 14 fonts, which should be renderable by all PDF readers. But this is not guaranteed.
            // For instance chrome does not seem to support it on Mac (March 2018). Therefore we only get it by font name if font embedded is disabled, which causes iText to no embed the font
            return FontManager.getFontName(fontFamily, fontStyle);
        } else {
            return FontManager.getFontPath(fontFamily, fontStyle);
        }
    }
}
