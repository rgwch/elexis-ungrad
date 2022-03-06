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

import ch.codeblock.qrinvoice.TechnicalException;
import ch.codeblock.qrinvoice.config.SystemProperties;
import ch.codeblock.qrinvoice.fonts.FontManager;
import ch.codeblock.qrinvoice.fonts.FontStyle;
import ch.codeblock.qrinvoice.graphics.Scissor;
import ch.codeblock.qrinvoice.image.ImageSupport;
import ch.codeblock.qrinvoice.layout.Dimension;
import ch.codeblock.qrinvoice.layout.LayoutException;
import ch.codeblock.qrinvoice.layout.Point;
import ch.codeblock.qrinvoice.layout.Rect;
import ch.codeblock.qrinvoice.model.*;
import ch.codeblock.qrinvoice.model.util.AddressUtils;
import ch.codeblock.qrinvoice.model.util.AlternativeSchemesUtils;
import ch.codeblock.qrinvoice.output.PaymentPartReceipt;
import ch.codeblock.qrinvoice.qrcode.SwissQrCode;
import ch.codeblock.qrinvoice.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.codeblock.qrinvoice.PageSize.DIN_LANG;
import static ch.codeblock.qrinvoice.PageSize.DIN_LANG_CROPPED;
import static ch.codeblock.qrinvoice.layout.DimensionUnitUtils.millimetersToPointsRounded;
import static ch.codeblock.qrinvoice.layout.DimensionUnitUtils.pointsToPixels;
import static ch.codeblock.qrinvoice.model.util.AddressUtils.toAddressLines;
import static ch.codeblock.qrinvoice.paymentpartreceipt.LayoutDefinitions.*;
import static ch.codeblock.qrinvoice.paymentpartreceipt.PaymentPartReceiptLayoutHelper.compressedLayout;
import static ch.codeblock.qrinvoice.paymentpartreceipt.PaymentPartReceiptLayoutHelper.uncompressedLayout;

public class Java2dPaymentPartReceiptWriter implements IPaymentPartReceiptWriter {
    private final Dimension<Integer> pageCanvas;
    /**
     * root lower left coordinates of the payment part on the pageCanvas in order to position the elements. coordinates are bottom left oriented. lower left is no always (0,0)
     */
    private final Point<Integer> rootLowerLeft = new Point<>(0, 0);
    private final Point<Integer> paymentPartLowerLeft;

    private final Font titleFont;
    private final Font separationLabelFont;
    private final Font paymentPartHeadingFont;
    private final Font paymentPartValueFont;
    private final Font paymentPartFurtherInformationFont;
    private final Font paymentPartFurtherInformationFontBold;
    private final Font receiptHeadingFont;
    private final Font receiptValueFont;

    private final int paymentPartFurtherInfoLineSpacing;
    private final int paymentPartAmountLineSpacing;
    private final int paymentPartParagraphSpacing;
    private final int paymentPartValueLineSpacing;
    private final int paymentPartHeadingLineSpacing;
    private final int receiptAmountLineSpacing;
    private final int receiptParagraphSpacing;
    private final int receiptValueLineSpacing;
    private final int receiptHeadingLineSpacing;

    private final HashMap<AttributedCharacterIterator.Attribute, Object> receiptValueTextAttributes;
    private final HashMap<AttributedCharacterIterator.Attribute, Object> paymentPartValueTextAttributes;

    private final Dimension<Integer> receiptPx;
    private final Dimension<Integer> paymentPartPx;

    private final int quietSpacePx;
    private final int additionalPrintMarginPx;
    private final int printMarginPx;
    private final int boundaryLineWidth;
    private final int boxCornerLineLength;
    private final int boxCornerLineWidth;

    private final Dimension<Integer> receiptDebtorField;
    private final Dimension<Integer> receiptAmountField;

    private final Rect<Integer> receiptTitleSectionRect;
    private final Rect<Integer> receiptInformationSectionRect;
    private final Rect<Integer> receiptAmountSectionRect;
    private final Rect<Integer> receiptAcceptanceSectionRect;

    private final Dimension<Integer> paymentPartAmountField;
    private final Dimension<Integer> paymentPartDebtorField;

    private final Rect<Integer> qrCodeRectangle;
    private final Rect<Integer> paymentPartTitleSectionRect;
    private final Rect<Integer> paymentPartQrSectionRect;
    private final Rect<Integer> paymentPartInformationSectionRect;
    private final Rect<Integer> paymentPartAmountSectionRect;
    private final Rect<Integer> paymentPartFurtherInfoSectionRect;

    private final ResourceBundle labels;
    private final PaymentPartReceiptWriterOptions options;
    private final Logger logger = LoggerFactory.getLogger(Java2dPaymentPartReceiptWriter.class);

    // DPI is key and drives all pixel sizes and coordinates
    private final int dpi;

    public Java2dPaymentPartReceiptWriter(final PaymentPartReceiptWriterOptions options) {
        this.options = options;
        this.labels = ResourceBundle.getBundle("qrinvoice", options.getLocale());
        this.dpi = options.getOutputResolution().getDpi();

        switch (options.getPageSize()) {
            case A4:
                pageCanvas = new Dimension<>(millimetersToPointsRounded(210, dpi), millimetersToPointsRounded(297, dpi));
                break;
            case A5:
                pageCanvas = new Dimension<>(millimetersToPointsRounded(210, dpi), millimetersToPointsRounded(148, dpi));
                break;
            case DIN_LANG:
            case DIN_LANG_CROPPED:
            default:
                pageCanvas = millimetersToPointsRounded(LayoutDefinitions.RECEIPT_PAYMENT_PART, dpi);
                break;
        }

        // -----
        // Fonts
        // -----
        final float fontSizeTitle = fontSizePointsToPixels(LayoutDefinitions.FONT_SIZE_TITLE);
        titleFont = FontManager.getFont(options.getFontFamily(), FontStyle.BOLD).deriveFont(fontSizeTitle);
        final float fontSizeSeparationLabel = fontSizePointsToPixels(LayoutDefinitions.FONT_SIZE_SEPARATION_LABEL);
        separationLabelFont = FontManager.getFont(options.getFontFamily(), FontStyle.REGULAR).deriveFont(fontSizeSeparationLabel);

        final float receiptFontSizeHeading = fontSizePointsToPixels(LayoutDefinitions.RECEIPT_FONT_SIZE_HEADING_MIN);
        final float receiptFontSizeValue = fontSizePointsToPixels(LayoutDefinitions.RECEIPT_FONT_SIZE_VALUE_MIN);

        receiptHeadingFont = FontManager.getFont(options.getFontFamily(), FontStyle.BOLD).deriveFont(receiptFontSizeHeading);
        receiptValueFont = FontManager.getFont(options.getFontFamily(), FontStyle.REGULAR).deriveFont(receiptFontSizeValue);

        receiptValueTextAttributes = new HashMap<>();
        receiptValueTextAttributes.put(TextAttribute.FONT, receiptValueFont);

        final float paymentPartFontSizeHeading = fontSizePointsToPixels(LayoutDefinitions.PAYMENT_PART_FONT_SIZE_HEADING_RECOMMENDED);
        final float paymentPartFontSizeValue = fontSizePointsToPixels(LayoutDefinitions.PAYMENT_PART_FONT_SIZE_VALUE_RECOMMENDED);
        final float paymentPartFontSizeFurtherInformation = fontSizePointsToPixels(LayoutDefinitions.PAYMENT_PART_FONT_SIZE_FURTHER_INFO);

        paymentPartHeadingFont = FontManager.getFont(options.getFontFamily(), FontStyle.BOLD).deriveFont(paymentPartFontSizeHeading);
        paymentPartValueFont = FontManager.getFont(options.getFontFamily(), FontStyle.REGULAR).deriveFont(paymentPartFontSizeValue);
        paymentPartFurtherInformationFontBold = FontManager.getFont(options.getFontFamily(), FontStyle.BOLD).deriveFont(paymentPartFontSizeFurtherInformation);
        paymentPartFurtherInformationFont = FontManager.getFont(options.getFontFamily(), FontStyle.REGULAR).deriveFont(paymentPartFontSizeFurtherInformation);

        paymentPartValueTextAttributes = new HashMap<>();
        paymentPartValueTextAttributes.put(TextAttribute.FONT, paymentPartValueFont);

        // -----
        // Line and paragraph spacings
        // -----
        paymentPartFurtherInfoLineSpacing = pointsToPixels(PAYMENT_PART_FURTHER_INFO_LINE_SPACING_PTS, dpi);
        paymentPartAmountLineSpacing = pointsToPixels(PAYMENT_PART_AMOUNT_LINE_SPACING_PTS, dpi);
        paymentPartParagraphSpacing = pointsToPixels(PAYMENT_PART_PARAGRAPH_SPACING_PTS, dpi);
        paymentPartValueLineSpacing = pointsToPixels(PAYMENT_PART_VALUE_LINE_SPACING_PTS, dpi);
        paymentPartHeadingLineSpacing = pointsToPixels(PAYMENT_PART_HEADING_LINE_SPACING_PTS, dpi);

        receiptAmountLineSpacing = pointsToPixels(RECEIPT_AMOUNT_LINE_SPACING_PTS, dpi);
        receiptParagraphSpacing = pointsToPixels(RECEIPT_PARAGRAPH_SPACING_PTS, dpi);
        receiptValueLineSpacing = pointsToPixels(RECEIPT_VALUE_LINE_SPACING_PTS, dpi);
        receiptHeadingLineSpacing = pointsToPixels(RECEIPT_HEADING_LINE_SPACING_PTS, dpi);

        // -----
        // init all dimensions
        // -----
        quietSpacePx = millimetersToPointsRounded(LayoutDefinitions.QUIET_SPACE, dpi);
        additionalPrintMarginPx = options.isAdditionalPrintMargin() ? millimetersToPointsRounded(LayoutDefinitions.ADDITIONAL_PRINT_MARGIN, dpi) : 0;
        printMarginPx = quietSpacePx + additionalPrintMarginPx;

        boundaryLineWidth = pointsToPixels(0.25f, dpi);
        boxCornerLineWidth = millimetersToPointsRounded(LayoutDefinitions.BOX_CORNER_LINE_WIDTH, dpi);
        boxCornerLineLength = millimetersToPointsRounded(LayoutDefinitions.BOX_CORNER_LINE_LENGTH, dpi);

        // -----
        // RECEIPT PART
        // -----
        receiptPx = millimetersToPointsRounded(LayoutDefinitions.RECEIPT, dpi);
        receiptDebtorField = mmToPixels(LayoutDefinitions.RECEIPT_DEBTOR_FIELD);
        receiptAmountField = mmToPixels(LayoutDefinitions.RECEIPT_AMOUNT_FIELD);

        final Dimension<Integer> receiptTitleSection = mmToPixels(LayoutDefinitions.RECEIPT_TITLE_SECTION);
        final Dimension<Integer> receiptInformationSection = mmToPixels(LayoutDefinitions.RECEIPT_INFORMATION_SECTION);
        final Dimension<Integer> receiptAmountSection = mmToPixels(LayoutDefinitions.RECEIPT_AMOUNT_SECTION);
        final Dimension<Integer> receiptAcceptanceSection = mmToPixels(LayoutDefinitions.RECEIPT_ACCEPTANCE_SECTION);

        receiptAcceptanceSectionRect = Rect.createUsingDimension(quietSpacePx, quietSpacePx, receiptAcceptanceSection.getWidth(), receiptAcceptanceSection.getHeight())
                .move(rootLowerLeft);

        final int receiptInfoSectX = quietSpacePx;
        final int receiptInfoSecty = quietSpacePx + receiptAcceptanceSection.getHeight() + receiptAmountSection.getHeight();
        receiptInformationSectionRect = Rect.createUsingDimension(receiptInfoSectX, receiptInfoSecty, receiptInformationSection.getWidth(), receiptInformationSection.getHeight())
                .move(rootLowerLeft);

        final int receiptAmountSectX = quietSpacePx;
        final int receiptAmountSectY = quietSpacePx + receiptAcceptanceSection.getHeight();
        receiptAmountSectionRect = Rect.createUsingDimension(receiptAmountSectX, receiptAmountSectY, receiptAmountSection.getWidth(), receiptAmountSection.getHeight())
                .move(rootLowerLeft);

        final int receiptTitleSectX = quietSpacePx;
        final int receiptTitleSectY = quietSpacePx + receiptAcceptanceSection.getHeight() + receiptAmountSection.getHeight() + receiptInformationSection.getHeight();
        receiptTitleSectionRect = Rect.createUsingDimension(receiptTitleSectX, receiptTitleSectY, receiptTitleSection.getWidth(), receiptTitleSection.getHeight())
                .move(rootLowerLeft);

        // -----
        // PAYMENT PART
        // -----
        paymentPartLowerLeft = rootLowerLeft.move(getReceiptWidth(), 0);
        paymentPartPx = millimetersToPointsRounded(LayoutDefinitions.PAYMENT_PART, dpi);
        paymentPartDebtorField = mmToPixels(LayoutDefinitions.PAYMENT_PART_DEBTOR_FIELD);
        paymentPartAmountField = mmToPixels(LayoutDefinitions.PAYMENT_PART_AMOUNT_FIELD);

        final Dimension<Integer> paymentPartTitleSection = mmToPixels(LayoutDefinitions.PAYMENT_PART_TITLE_SECTION);
        final Dimension<Integer> paymentPartQrSection = mmToPixels(LayoutDefinitions.PAYMENT_PART_QR_SECTION);
        final Dimension<Integer> paymentPartAmountSection = mmToPixels(LayoutDefinitions.PAYMENT_PART_AMOUNT_SECTION);
        final Dimension<Integer> paymentPartInformationSection = mmToPixels(LayoutDefinitions.PAYMENT_PART_INFORMATION_SECTION);
        final Dimension<Integer> paymentPartFurtherInfoSection = mmToPixels(LayoutDefinitions.PAYMENT_PART_FURTHER_INFO_SECTION);

        final int paymentPartTitleSectX = quietSpacePx;
        final int paymentPartTitleSectY = getPaymentPartHeight() - quietSpacePx - paymentPartTitleSection.getHeight();
        paymentPartTitleSectionRect = Rect.createUsingDimension(paymentPartTitleSectX, paymentPartTitleSectY, paymentPartTitleSection.getWidth(), paymentPartTitleSection.getHeight())
                .move(paymentPartLowerLeft);

        paymentPartFurtherInfoSectionRect = Rect.createUsingDimension(quietSpacePx, quietSpacePx, paymentPartFurtherInfoSection.getWidth(), paymentPartFurtherInfoSection.getHeight())
                .move(paymentPartLowerLeft);

        final int paymentPartAmountSectX = quietSpacePx;
        final int paymentPartAmountSectY = paymentPartFurtherInfoSectionRect.getTopY();
        paymentPartAmountSectionRect = Rect.createUsingDimension(paymentPartAmountSectX, paymentPartAmountSectY, paymentPartAmountSection.getWidth(), paymentPartAmountSection.getHeight())
                .move(paymentPartLowerLeft);

        // QR Code Rectangle
        final Integer paymentPartQrSectX = quietSpacePx;
        final Integer paymentPartQrSectY = paymentPartAmountSectionRect.getTopY();
        paymentPartQrSectionRect = Rect.createUsingDimension(paymentPartQrSectX, paymentPartQrSectY, paymentPartQrSection.getWidth(), paymentPartQrSection.getHeight())
                .move(paymentPartLowerLeft);
        final int qrCodeX = quietSpacePx;
        final int qrCodeY = paymentPartQrSectionRect.getBottomY() + quietSpacePx;
        qrCodeRectangle = Rect.createUsingDimension(qrCodeX, qrCodeY, mmToPixels(SwissQrCode.QR_CODE_SIZE)).move(paymentPartLowerLeft);

        // Payment Part Information Section Rectangle
        final int paymentPartInfoSectX = paymentPartTitleSectionRect.getRightX();
        final int paymentPartInfoSectY = paymentPartFurtherInfoSectionRect.getTopY();
        paymentPartInformationSectionRect = Rect.createUsingDimension(paymentPartInfoSectX, paymentPartInfoSectY, paymentPartInformationSection.getWidth(), paymentPartInformationSection.getHeight());
    }

    private int fontSizePointsToPixels(final int fontSize) {
        return Math.floorDiv(fontSize * dpi, 72);
    }

    private int getPageCanvasWidth() {
        return pageCanvas.getWidth();
    }

    private int getPageCanvasHeight() {
        return pageCanvas.getHeight();
    }


    private Dimension<Integer> mmToPixels(final Dimension<Float> mmDimension) {
        return new Dimension<>(millimetersToPointsRounded(mmDimension.getWidth(), dpi), millimetersToPointsRounded(mmDimension.getHeight(), dpi));
    }


    private int getPaymentPartWidth() {
        return paymentPartPx.getWidth();
    }

    private int getPaymentPartHeight() {
        return paymentPartPx.getHeight();
    }

    private int getReceiptWidth() {
        return receiptPx.getWidth();
    }

    @Override
    public int getQrCodeImageSize() {
        final float mm = SwissQrCode.QR_CODE_SIZE.getWidth();
        final float mmPerInch = 25.4f; // 1 inch = 2.54 cm => 25.4mm
        // TODO
        return Math.round(600 * mm / mmPerInch); // pixels
    }

    @Override
    public ch.codeblock.qrinvoice.output.PaymentPartReceipt write(final QrInvoice qrInvoice, final BufferedImage qrCodeImage) {
        final BufferedImage img = new BufferedImage(getPageCanvasWidth(), getPageCanvasHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = null;
        try {
            g = img.createGraphics();
            g.setBackground(Color.WHITE);
            g.fillRect(0, 0, getPageCanvasWidth(), getPageCanvasHeight());

            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED); // we dont need alpha channel
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // quality is important for readabilty of both text and QR Code
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED); // we don't have "colors", only greyscale
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g.setColor(Color.BLACK);

            addReceiptTitleSection(g);
            addReceiptInformationSection(g, qrInvoice);
            addReceiptAmount(g, qrInvoice.getPaymentAmountInformation());
            addReceiptAcceptanceSection(g);

            addPaymentPartTitleSection(g);
            addPaymentPartQrCodeImage(g, qrCodeImage);
            addPaymentPartAmount(g, qrInvoice.getPaymentAmountInformation());
            addPaymentPartInformationSection(g, qrInvoice);
            addPaymentPartFurtherInfoSection(g, qrInvoice);

            drawBoundaryLines(g);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }

        return writeImage(img);
    }

    public PaymentPartReceipt writeImage(final BufferedImage img) {
        try {
            final BufferedImage resultImage = cropImageIfNeeded(img);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
            new ImageSupport().write(options.getOutputFormat(), baos, resultImage);
            return new PaymentPartReceipt(options.getPageSize(), options.getOutputFormat(), baos.toByteArray(), getPageCanvasWidth(), getPageCanvasHeight());
        } catch (IOException ex) {
            throw new TechnicalException(ex);
        }
    }

    private BufferedImage cropImageIfNeeded(final BufferedImage src) {
        if(options.getPageSize().greaterThan(DIN_LANG_CROPPED)) {
            return src;
        }

        Graphics2D g = null;
        try {
            /*
             * Basically the following cropping is done. Remove print margin on the left, the right and at the bottom.
             * +--------------------------------------+
             * |  |                                |  |
             * |  |        Cropped DIN LANG        |  |
             * |  |                                |  |
             * |  +--------------------------------+  |
             * |                                      |
             * +--------------------------------------+
             */
            final BufferedImage dest = new BufferedImage(src.getWidth() - 2 * printMarginPx, src.getHeight() - printMarginPx, BufferedImage.TYPE_BYTE_GRAY);
            g = dest.createGraphics();
            g.drawImage(src, 0, 0, dest.getWidth(), dest.getHeight(), printMarginPx, 0, src.getWidth() - printMarginPx, src.getHeight() - printMarginPx, null);
            return dest;
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    private void addReceiptTitleSection(final Graphics2D g) {
        debugLayout(g, receiptTitleSectionRect);

        g.setFont(titleFont);
        g.drawString(getLabel("TitleReceipt"), receiptTitleSectionRect.getLeftX() + additionalPrintMarginPx, translateY(receiptTitleSectionRect.getTopY() - currentAscent(g)));
    }

    private void addReceiptInformationSection(final Graphics2D g, final QrInvoice qrInvoice) {
        // in the information section y position is always treated from top to down
        final int bottomY = translateY(receiptInformationSectionRect.getBottomY());
        final Integer leftX = receiptInformationSectionRect.getLeftX() + additionalPrintMarginPx;
        final Integer topY = translateY(receiptInformationSectionRect.getTopY());
        debugLayout(g, receiptInformationSectionRect);


        // According to the spec 3.6.2 - Information section - v2.0
        // "Because of the limited space, it is permitted to omit the street name and building number from the addresses of the creditor (Payable to) and the debtor (Payable by)"
        boolean omitStreetNameHouseNumberOrAddressLine1 = compressedLayout(options.getLayout());

        // Account
        AtomicInteger yPos = new AtomicInteger(topY);
        writeReceiptInformationSectionTitle(g, yPos, getLabel("CdtrInf.IBANCreditor"), true);
        writeReceiptInformationSectionValue(g, yPos, IbanUtils.formatIban(qrInvoice.getCreditorInformation().getIban()));

        // Creditor
        for (String addressLine : toAddressLines(qrInvoice.getCreditorInformation().getCreditor(), omitStreetNameHouseNumberOrAddressLine1)) {
            writeReceiptInformationSectionValue(g, yPos, addressLine);
        }

        final PaymentReference paymentReference = qrInvoice.getPaymentReference();
        if (paymentReference != null) {
            // Reference number
            if (paymentReference.getReferenceType() != null) {
                switch (paymentReference.getReferenceType()) {
                    case QR_REFERENCE:
                    case CREDITOR_REFERENCE:
                        if (uncompressedLayout(options.getLayout())) {
                            yPos.addAndGet(receiptParagraphSpacing);
                        }
                        writeReceiptInformationSectionTitle(g, yPos, getLabel("RmtInf.Ref"), false);
                        writeReceiptInformationSectionValue(g, yPos, ReferenceUtils.format(paymentReference.getReferenceType(), paymentReference.getReference()));
                        break;
                    case WITHOUT_REFERENCE:
                        break;
                }
            }
        }

        // Debtor
        if (uncompressedLayout(options.getLayout())) {
            yPos.addAndGet(receiptParagraphSpacing);
        }
        if (AddressUtils.isEmpty(qrInvoice.getUltimateDebtor())) {
            writeReceiptInformationSectionTitle(g, yPos, getLabel("UltmtDbtr.Empty"), false);
            final int lowerY = translateY(yPos.addAndGet(receiptDebtorField.getHeight()));
            final Rect<Integer> debtorFieldRect = receiptDebtorField.toRectangle(leftX, lowerY);
            writeFreeTextBox(g, debtorFieldRect);
        } else {
            writeReceiptInformationSectionTitle(g, yPos, getLabel("UltmtDbtr"), false);
            for (String addressLine : toAddressLines(qrInvoice.getUltimateDebtor(), omitStreetNameHouseNumberOrAddressLine1)) {
                writeReceiptInformationSectionValue(g, yPos, addressLine);
            }

            // this is relevant as yPos is check to be within the allowed layout section - but last line spacing was added for next line, which is not used
            yPos.addAndGet(-receiptValueLineSpacing);
        }

        if (yPos.get() > bottomY) {
            final String message = "Not all content could be printed to receipt information section";
            // nok
            if (System.getProperty(SystemProperties.IGNORE_LAYOUT_ERRORS) == null) {
                throw new LayoutException(message);
            } else {
                logger.info(message);
            }
        }
    }

    private void addReceiptAmount(final Graphics2D g, final PaymentAmountInformation paymentAmountInformation) {
        debugLayout(g, receiptAmountSectionRect);

        final int upperY = receiptAmountSectionRect.getUpperRightY();

        // header
        g.setFont(receiptHeadingFont);
        final int currencyX = receiptAmountSectionRect.getLeftX() + additionalPrintMarginPx;
        final int amountX = currencyX + millimetersToPointsRounded(12, dpi);
        final int headerY = translateY(upperY - currentAscent(g));
        g.drawString(getLabel("Currency"), currencyX, headerY);
        g.drawString(getLabel("Amount"), amountX, headerY);

        // body
        final int valuesY = headerY + receiptValueFont.getSize() + receiptAmountLineSpacing;
        g.setFont(receiptValueFont);
        g.drawString(paymentAmountInformation.getCurrency().getCurrencyCode(), currencyX, valuesY);
        if (paymentAmountInformation.getAmount() != null) {
            final String formattedAmount = DecimalFormatFactory.createPrintAmountFormat().format(paymentAmountInformation.getAmount());
            g.drawString(formattedAmount, amountX, valuesY);
        }

        if (paymentAmountInformation.getAmount() == null) {
            final int amountFieldX = receiptAmountSectionRect.getRightX() - receiptAmountField.getWidth() + additionalPrintMarginPx;
            final int amountFieldY = upperY - receiptAmountField.getHeight();
            final Rect<Integer> rect = receiptAmountField.toRectangle(amountFieldX, amountFieldY);
            writeFreeTextBox(g, rect);
        }
    }

    private void addReceiptAcceptanceSection(final Graphics2D g) {
        debugLayout(g, receiptAcceptanceSectionRect);

        final String acceptancePoint = getLabel("AcceptancePoint");

        g.setFont(receiptHeadingFont);
        final int textWidth = g.getFontMetrics().stringWidth(acceptancePoint);

        final int y = translateY(receiptAcceptanceSectionRect.getTopY() - currentAscent(g));
        g.drawString(acceptancePoint, receiptAcceptanceSectionRect.getRightX() - textWidth, y);
    }

    private int currentDescent(final Graphics2D g) {
        return g.getFontMetrics().getDescent();
    }

    private int currentAscent(final Graphics2D g) {
        return normalizedAscent(g.getFontMetrics());
    }

    /**
     * Ascent normalization is needed because in some cases Arial (or at least some arial variants, and maybe other fonts too) have higher ascents than they require.<br>
     * If we do not normalize it, spacing is much different between Arial and Helvetica for example.<br>
     * This is a bit of a hack, maybe we switch to static line pacing instead, however basing on font metrics is conceptionally the better approach
     */
    private int normalizedAscent(FontMetrics metrics) {
        final int ascent = metrics.getAscent();
        final int descent = metrics.getDescent();
        final int size = metrics.getFont().getSize();
        // magic number - as ascent + descent does not match font size, we correct it a bit here, the value was determined by checking the outputs and correct it accordingly
        final int correctionMargin = pointsToPixels(0.25f, dpi);
        logger.trace("ascent: {}, descent: {}, font-size: {}", ascent, descent, size);
        logger.trace("correction margin: {}", correctionMargin);
        if ((ascent + descent) > (size + correctionMargin)) {
            final int normalizedAscent = size - descent + correctionMargin;
            logger.trace("ascent normalized to: {}", normalizedAscent);
            return normalizedAscent;
        } else {
            logger.trace("ascent not normalized");
            return ascent;
        }
    }

    private void addPaymentPartTitleSection(final Graphics2D g) {
        debugLayout(g, paymentPartTitleSectionRect);

        g.setFont(titleFont);
        g.drawString(getLabel("TitlePaymentPart"), paymentPartTitleSectionRect.getLeftX(), translateY(paymentPartTitleSectionRect.getTopY() - currentAscent(g)));
    }

    private void addPaymentPartQrCodeImage(final Graphics2D g, final BufferedImage qrCodeImage) {
        debugLayout(g, qrCodeRectangle);

        final Rect<Integer> absolutePos = qrCodeRectangle;
        final int x = absolutePos.getLeftX();
        final int y = translateY(absolutePos.getTopY());

        // TODO test if a "not 100% exact"-scale could solve some sharpness issues
        g.drawImage(qrCodeImage, x, y, qrCodeRectangle.getWidth(), qrCodeRectangle.getHeight(), null);

        debugLayout(g, paymentPartQrSectionRect);

    }

    private void addPaymentPartAmount(final Graphics2D g, final PaymentAmountInformation paymentAmountInformation) {
        debugLayout(g, paymentPartAmountSectionRect);
        final int lowerY = paymentPartAmountSectionRect.getBottomY();
        final int upperY = paymentPartAmountSectionRect.getTopY();

        // header
        g.setFont(paymentPartHeadingFont);
        final int currencyX = paymentPartLowerLeft.getX() + quietSpacePx;
        final int amountX = paymentPartLowerLeft.getX() + quietSpacePx + millimetersToPointsRounded(14, dpi);
        final int headerY = translateY(upperY - currentAscent(g));
        g.drawString(getLabel("Currency"), currencyX, headerY);
        g.drawString(getLabel("Amount"), amountX, headerY);

        // body
        final int valuesY = headerY + paymentPartValueFont.getSize() + paymentPartAmountLineSpacing;
        g.setFont(paymentPartValueFont);
        g.drawString(paymentAmountInformation.getCurrency().getCurrencyCode(), currencyX, valuesY);
        if (paymentAmountInformation.getAmount() != null) {
            final String formattedAmount = DecimalFormatFactory.createPrintAmountFormat().format(paymentAmountInformation.getAmount());
            g.drawString(formattedAmount, amountX, valuesY);
        }

        if (paymentAmountInformation.getAmount() == null) {
            final int amountFieldX = paymentPartAmountSectionRect.getRightX() - paymentPartAmountField.getWidth() - millimetersToPointsRounded(1, dpi);
            final int amountFieldY = lowerY + millimetersToPointsRounded(2.8f, dpi);
            final Rect<Integer> rect = paymentPartAmountField.toRectangle(amountFieldX, amountFieldY);
            writeFreeTextBox(g, rect);
        }
    }

    private void addPaymentPartInformationSection(final Graphics2D g, final QrInvoice qrInvoice) {
        // in the information section y position is always treated from top to down
        final int bottomY = translateY(paymentPartInformationSectionRect.getBottomY());
        final Integer leftX = paymentPartInformationSectionRect.getLeftX();
        final Integer topY = translateY(paymentPartInformationSectionRect.getTopY());
        debugLayout(g, paymentPartInformationSectionRect);

        // Account
        AtomicInteger yPos = new AtomicInteger(topY);
        writePaymentPartInformationSectionTitle(g, yPos, getLabel("CdtrInf.IBANCreditor"), true);
        writePaymentPartInformationSectionValue(g, yPos, IbanUtils.formatIban(qrInvoice.getCreditorInformation().getIban()));

        // Creditor
        for (String addressLine : toAddressLines(qrInvoice.getCreditorInformation().getCreditor())) {
            writePaymentPartInformationSectionValue(g, yPos, addressLine);
        }

        final PaymentReference paymentReference = qrInvoice.getPaymentReference();
        if (paymentReference != null) {
            // Reference number
            if (paymentReference.getReferenceType() != null) {
                switch (paymentReference.getReferenceType()) {
                    case QR_REFERENCE:
                    case CREDITOR_REFERENCE:
                        if (uncompressedLayout(options.getLayout())) {
                            yPos.addAndGet(paymentPartParagraphSpacing);
                        }
                        writePaymentPartInformationSectionTitle(g, yPos, getLabel("RmtInf.Ref"), false);
                        writePaymentPartInformationSectionValue(g, yPos, ReferenceUtils.format(paymentReference.getReferenceType(), paymentReference.getReference()));
                        break;
                    case WITHOUT_REFERENCE:
                        break;
                }
            }

            // Additional information
            final String unstructuredMessage = DoNotUseForPayment.localize(paymentReference.getAdditionalInformation().getUnstructuredMessage(), options.getLocale());
            final String billInformation = paymentReference.getAdditionalInformation().getBillInformation();
            if (unstructuredMessage != null || billInformation != null) {
                if (uncompressedLayout(options.getLayout())) {
                    yPos.addAndGet(paymentPartParagraphSpacing);
                }

                writePaymentPartInformationSectionTitle(g, yPos, getLabel("RmtInf.AddInf.Ustrd"), false);
                if ((compressedLayout(options.getLayout())) && ((StringUtils.length(unstructuredMessage, billInformation)) > (2 * APPROX_MAX_LINE_LENGTH_PAYMENT_PART_INFO_SECTION))) {
                    // according to the spec version 2.0 - 3.5.4:
                    // If both elements are filled in, then a line break can be introduced after the information in the first element "Ustrd" (Unstructured message). 
                    // If there is insufficient space, the line break can be omitted (but this makes it more difficult to read).
                    // If not all the details contained in the QR code can be displayed, the shortened content must be marked with "..." at the end. It must be ensured
                    // that all personal data is displayed.
                    final String joined = StringUtils.join(" ", unstructuredMessage, billInformation);
                    writePaymentPartInformationSectionValue(g, yPos, joined);
                } else {
                    if (unstructuredMessage != null && !unstructuredMessage.isEmpty()) {
                        writePaymentPartInformationSectionValue(g, yPos, unstructuredMessage);
                    }
                    if (billInformation != null && !billInformation.isEmpty()) {
                        writePaymentPartInformationSectionValue(g, yPos, billInformation);
                    }
                }
            }
        }

        // Debtor
        if (uncompressedLayout(options.getLayout())) {
            yPos.addAndGet(paymentPartParagraphSpacing);
        }
        if (AddressUtils.isEmpty(qrInvoice.getUltimateDebtor())) {
            writePaymentPartInformationSectionTitle(g, yPos, getLabel("UltmtDbtr.Empty"), false);
            final int lowerY = translateY(yPos.addAndGet(paymentPartDebtorField.getHeight()));
            final Rect<Integer> debtorFieldRect = paymentPartDebtorField.toRectangle(leftX, lowerY);
            writeFreeTextBox(g, debtorFieldRect);
        } else {
            writePaymentPartInformationSectionTitle(g, yPos, getLabel("UltmtDbtr"), false);
            for (String addressLine : toAddressLines(qrInvoice.getUltimateDebtor())) {
                writePaymentPartInformationSectionValue(g, yPos, addressLine);
            }

            // this is relevant as yPos is check to be within the allowed layout section - but last line spacing was added for next line, which is not used
            yPos.addAndGet(-paymentPartValueLineSpacing);
        }

        if (yPos.get() > bottomY) {
            if (((yPos.get() - bottomY) <= millimetersToPointsRounded(1, dpi))) {
                // edge test data case handling -> 1mm overlap allowed
                return;
            }
            // nok
            if (System.getProperty(SystemProperties.IGNORE_LAYOUT_ERRORS) == null) {
                throw new LayoutException("Not all content could be printed to payment part information section");
            }
        }
    }

    private void addPaymentPartFurtherInfoSection(final Graphics2D g, final QrInvoice qrInvoice) {
        debugLayout(g, paymentPartFurtherInfoSectionRect);

        // alternative schemes
        g.setFont(paymentPartFurtherInformationFontBold);
        final int lowerY = paymentPartFurtherInfoSectionRect.getBottomY() + additionalPrintMarginPx + currentDescent(g);
        int y = translateY(lowerY);

        if (qrInvoice.getAlternativeSchemes() != null && qrInvoice.getAlternativeSchemes().getAlternativeSchemeParameters() != null) {
            final List<String> alternativeSchemes = new ArrayList<>(qrInvoice.getAlternativeSchemes().getAlternativeSchemeParameters());
            Collections.reverse(alternativeSchemes);
            for (final String alternativeScheme : alternativeSchemes) {
                final AlternativeSchemesUtils.AlternativeSchemePair scheme = AlternativeSchemesUtils.parseForOutput(alternativeScheme);
                if (!scheme.isEmpty()) {
                    final int x = paymentPartFurtherInfoSectionRect.getLeftX();
                    if (scheme.hasName()) {
                        // 3.4 - font size - v2.0: When filling in the "Alternative procedures" element, the font size is 7 pt, with the name of the alternative procedure printed in bold.
                        g.setFont(paymentPartFurtherInformationFontBold);
                        g.drawString(scheme.getName(), x, y);
                        if (scheme.hasValue()) {
                            final float widthName = g.getFontMetrics().stringWidth(scheme.getName());
                            final float remainingSpaceValue = paymentPartFurtherInfoSectionRect.getWidth() - widthName;
                            final String value = trimIfRequired(scheme.getValue(), remainingSpaceValue, paymentPartFurtherInformationFont, g);

                            g.setFont(paymentPartFurtherInformationFont);
                            final float xValue = x + widthName;
                            g.drawString(value, xValue, y);
                        }
                    } else {
                        final String value = trimIfRequired(scheme.getValue(), paymentPartFurtherInfoSectionRect.getWidth(), paymentPartFurtherInformationFont, g);

                        g.setFont(paymentPartFurtherInformationFont);
                        g.drawString(value, x, y);
                    }

                    y -= currentAscent(g) + currentDescent(g);
                    y -= paymentPartFurtherInfoLineSpacing;
                }
            }
        }

        // Ultimate Creditor / FINAL Creditor - according to the spec 3.5.5 - further information section:
        // "This section is where the "Final creditor" field, if available and approved for use, is displayed. 
        // Instead of the designation "Final creditor", the relevant values in the Swiss QR Code are preceded by the words "In favour of" (bold). 
        // Just one line is available, so it is possible that not all the information in the QR-bill can be printed there. 
        // If that is the case, the shortened entry must be marked by "..." at the end. The data is printed in font size 7 pt, in the same order as in the Swiss QR Code."

        final UltimateCreditor ultimateCreditor = qrInvoice.getUltimateCreditor();
        if (!AddressUtils.isEmpty(ultimateCreditor)) {
            final float x = paymentPartFurtherInfoSectionRect.getLeftX();
            final String ultmtCdtrLabel = getLabel("UltmtCdtr") + " ";
            g.setFont(paymentPartFurtherInformationFontBold);
            g.drawString(ultmtCdtrLabel, x, y);

            final float widthLabel = g.getFontMetrics().stringWidth(ultmtCdtrLabel);
            final float remainingWidthAddressLine = paymentPartFurtherInfoSectionRect.getWidth() - widthLabel;
            final String addressLine = trimIfRequired(AddressUtils.toSingleLineAddress(ultimateCreditor), remainingWidthAddressLine, paymentPartFurtherInformationFont, g);

            final float xUltimateCreditorValue = x + widthLabel;
            g.setFont(paymentPartFurtherInformationFont);
            g.drawString(addressLine, xUltimateCreditorValue, y);
        }
    }

    private void writeReceiptInformationSectionTitle(final Graphics2D g, final AtomicInteger yPos, final String text, final boolean firstTitle) {
        g.setFont(receiptHeadingFont);
        if (!firstTitle) {
            yPos.addAndGet(receiptHeadingLineSpacing);
        }
        final int textBaselineY = yPos.addAndGet(currentAscent(g));
        g.drawString(text, receiptInformationSectionRect.getLeftX() + additionalPrintMarginPx, textBaselineY);
        yPos.addAndGet(currentDescent(g));
        yPos.addAndGet(receiptValueLineSpacing);
    }


    private void writeReceiptInformationSectionValue(final Graphics2D g, final AtomicInteger yPos, final String text) {
        g.setFont(receiptValueFont);

        final AttributedString attributedString = new AttributedString(text, receiptValueTextAttributes);
        final AttributedCharacterIterator paragraph = attributedString.getIterator();

        final int breakWidth = receiptInformationSectionRect.getWidth() - additionalPrintMarginPx;

        // try first with a word-based wrapping
        LineBreakMeasurer lineMeasurer = createLineBreakMeasurer(g, paragraph, BreakIterator.getWordInstance(options.getLocale()));
        List<TextLayout> layouts = getLayouts(paragraph, breakWidth, lineMeasurer);

        if (layouts.size() > 2) {
            // there are more than two lines, thus applying aggressive splitting without further checks
            logger.debug("applying aggressive splitting");
            // throw away and recreate
            lineMeasurer = createLineBreakMeasurer(g, paragraph, BreakIterator.getCharacterInstance(options.getLocale()));
            layouts = getLayouts(paragraph, breakWidth, lineMeasurer);
        }

        for (final TextLayout layout : layouts) {
            final int textBaselineY = yPos.addAndGet(currentAscent(g));
            layout.draw(g, receiptInformationSectionRect.getLeftX() + additionalPrintMarginPx, textBaselineY);
            // Move y-coordinate in preparation for next layout.
            yPos.addAndGet(currentDescent(g));
            yPos.addAndGet(receiptValueLineSpacing);
        }
    }

    private void writePaymentPartInformationSectionTitle(final Graphics2D g, final AtomicInteger yPos, final String text, final boolean firstTitle) {
        g.setFont(paymentPartHeadingFont);
        if (!firstTitle) {
            yPos.addAndGet(paymentPartHeadingLineSpacing);
        }
        final int textBaselineY = yPos.addAndGet(currentAscent(g));
        g.drawString(text, paymentPartInformationSectionRect.getLeftX(), textBaselineY);
        yPos.addAndGet(currentDescent(g));
        yPos.addAndGet(paymentPartValueLineSpacing);
    }

    private void writePaymentPartInformationSectionValue(final Graphics2D g, final AtomicInteger yPos, final String text) {
        g.setFont(paymentPartValueFont);

        final AttributedString attributedString = new AttributedString(text, paymentPartValueTextAttributes);
        final AttributedCharacterIterator paragraph = attributedString.getIterator();

        final int breakWidth = paymentPartInformationSectionRect.getWidth() - additionalPrintMarginPx;

        // try first with a word-based wrapping
        LineBreakMeasurer lineMeasurer = createLineBreakMeasurer(g, paragraph, BreakIterator.getWordInstance(options.getLocale()));
        List<TextLayout> layouts = getLayouts(paragraph, breakWidth, lineMeasurer);

        if (layouts.size() > 2) {
            // there are more than two lines, thus applying aggressive splitting without further checks
            logger.debug("applying aggressive splitting");
            // throw away and recreate
            lineMeasurer = createLineBreakMeasurer(g, paragraph, BreakIterator.getCharacterInstance(options.getLocale()));
            layouts = getLayouts(paragraph, breakWidth, lineMeasurer);
        }

        for (final TextLayout layout : layouts) {
            // Move y-coordinate by the ascent of the layout.
            // Draw the TextLayout at (drawPosX, drawPosY).
            final int textBaselineY = yPos.addAndGet(currentAscent(g));
            layout.draw(g, paymentPartInformationSectionRect.getLeftX(), textBaselineY);
            // Move y-coordinate in preparation for next layout.
            yPos.addAndGet(currentDescent(g));
            yPos.addAndGet(paymentPartValueLineSpacing);
        }
    }

    private List<TextLayout> getLayouts(final AttributedCharacterIterator paragraph, final int breakWidth, final LineBreakMeasurer lineMeasurer) {
        final List<TextLayout> layouts = new ArrayList<>();
        // Get lines until the entire paragraph has been rendered
        while (lineMeasurer.getPosition() < paragraph.getEndIndex()) {
            layouts.add(lineMeasurer.nextLayout(breakWidth));
        }
        return layouts;
    }

    private LineBreakMeasurer createLineBreakMeasurer(final Graphics2D g, final AttributedCharacterIterator paragraph, final BreakIterator breakIterator) {
        final FontRenderContext frc = g.getFontRenderContext();
        final LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, breakIterator, frc);
        lineMeasurer.setPosition(paragraph.getBeginIndex());
        return lineMeasurer;
    }


    private int translateY(final int y) {
        return NumberUtils.subtract(getPageCanvasHeight(), y);
    }

    private String getLabel(final String key) {
        return labels.getString(key);
    }


    /**
     * According to the spec 3.5.5 - Further information section - v2.0:
     * <p>"In the Swiss QR Code, there are always 100 alphanumerical characters available for the "Alternative procedures".
     * A maximum of approx. 90 characters can be printed on one line, so it is possible that not all the data included in the QR code can be displayed.
     * If that is the case, the shortened entry must be marked by "..." at the end. It must be ensured that all personal data is displayed."</p>
     *
     * @param maxWidth
     * @param font
     * @param g
     * @return
     */
    private String trimIfRequired(final String input, final float maxWidth, final Font font, final Graphics2D g) {
        final FontMetrics fontMetrics = g.getFontMetrics(font);
        float widthPoint = fontMetrics.stringWidth(input);
        if (widthPoint <= maxWidth) {
            return input;
        }

        String newString = input;
        while (widthPoint > maxWidth) {
            final int len = newString.length();
            final float ratioTooWide = widthPoint / maxWidth;
            // make sure string gets shorter in any circumstances
            final int newLen = Math.min((int) Math.floor(len / ratioTooWide), newString.length() - 1);

            newString = newString.substring(0, newLen);
            widthPoint = fontMetrics.stringWidth(newString + MORE_TEXT_INDICATOR);
        }

        return newString + MORE_TEXT_INDICATOR;
    }

    private void writeFreeTextBox(final Graphics2D g, final Rect<Integer> rect) {
        final float cornerLength = boxCornerLineLength;
        final float leftX = rect.getLowerLeftX();
        final float lowerY = translateY(rect.getLowerLeftY());
        final float rightX = rect.getUpperRightX();
        final float upperY = translateY(rect.getUpperRightY());
        final BasicStroke solid = new BasicStroke(boxCornerLineWidth);
        g.setStroke(solid);

        // left bottom
        final GeneralPath lowerLeftCorner = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        lowerLeftCorner.moveTo(leftX, lowerY - cornerLength);
        lowerLeftCorner.lineTo(leftX, lowerY);
        lowerLeftCorner.lineTo(leftX + cornerLength, lowerY);
        g.draw(lowerLeftCorner);

        // left top
        final GeneralPath upperLeftCorner = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        upperLeftCorner.moveTo(leftX, upperY + cornerLength);
        upperLeftCorner.lineTo(leftX, upperY);
        upperLeftCorner.lineTo(leftX + cornerLength, upperY);
        g.draw(upperLeftCorner);

        // right upper
        final GeneralPath upperRightCorner = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        upperRightCorner.moveTo(rightX, upperY + cornerLength);
        upperRightCorner.lineTo(rightX, upperY);
        upperRightCorner.lineTo(rightX - cornerLength, upperY);
        g.draw(upperRightCorner);

        // right bottom
        final GeneralPath lowerRightCorner = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        lowerRightCorner.moveTo(rightX, lowerY - cornerLength);
        lowerRightCorner.lineTo(rightX, lowerY);
        lowerRightCorner.lineTo(rightX - cornerLength, lowerY);
        g.draw(lowerRightCorner);
    }

    private void drawBoundaryLines(final Graphics2D g) {
        BoundaryLines boundaryLines = options.getBoundaryLines();

        if (boundaryLines == null) {
            return;
        }

        switch (boundaryLines) {
            case ENABLED:
            case ENABLED_WITH_MARGINS:
                writeSeparationLabel(g);
                final int halfLineWidth = Math.floorDiv(boundaryLineWidth, 2);
                drawHorizontalBoundaryLine(g, halfLineWidth, boundaryLines);
                drawVerticalBoundaryLine(g, halfLineWidth, boundaryLines);
                break;
        }
    }

    private void writeSeparationLabel(final Graphics2D g) {
        if (options.isBoundaryLineSeparationText() && options.getPageSize().greaterThan(ch.codeblock.qrinvoice.PageSize.DIN_LANG)) {
            g.setFont(separationLabelFont);
            final int x = paymentPartTitleSectionRect.getLeftX();
            g.drawString(getLabel("SeparationLabel"), x, translateY(getPaymentPartHeight() + currentAscent(g)));
        } else if (options.isBoundaryLineSeparationText()) {
            logger.warn("Separation label above payment part was not printed as PageSize is too small.");
        }
    }

    private void drawHorizontalBoundaryLine(final Graphics2D g, final int halfLineWidth, BoundaryLines boundaryLines) {
        final int leftX = rootLowerLeft.getX() + halfLineWidth;
        final int rightX = rootLowerLeft.getX() + getReceiptWidth() + getPaymentPartWidth();
        final int y = rootLowerLeft.getY() + getPaymentPartHeight() - halfLineWidth;

        final int leftXwithMargin = boundaryLines == BoundaryLines.ENABLED_WITH_MARGINS ? leftX + printMarginPx: leftX;
        final int rightXwithMargin = boundaryLines == BoundaryLines.ENABLED_WITH_MARGINS ? rightX - printMarginPx: rightX;

        g.setStroke(new BasicStroke(boundaryLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        final boolean printHorizontalScissor = options.isBoundaryLineScissors() && options.getPageSize().greaterThan(DIN_LANG);
        if (printHorizontalScissor) {
            final double scissorWidth = millimetersToPointsRounded(SCISSOR_LENGTH, dpi);
            final double scale = scissorWidth / Scissor.getOrigWidth();
            final int scissorHeight = (int) (Scissor.getOrigHeight() * scale);
            final int scissorUpperY = y + (scissorHeight / 2);

            AffineTransform at = new AffineTransform();
            final double scissorX = (double) leftXwithMargin + (double) quietSpacePx + additionalPrintMarginPx;
            at.translate(scissorX, translateY(scissorUpperY));
            at.scale(scale, scale);

            drawScissor(g, at);

            // two lines - leave out a space for the scissor
            final GeneralPath leftHorizontalLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            final int translatedY = translateY(y);
            leftHorizontalLine.moveTo(leftXwithMargin, translatedY);
            leftHorizontalLine.lineTo(scissorX, translatedY);
            g.draw(leftHorizontalLine);

            final GeneralPath rightHorizontalLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            rightHorizontalLine.moveTo(scissorX + scissorWidth, translatedY);
            rightHorizontalLine.lineTo(rightXwithMargin, translatedY);
            g.draw(rightHorizontalLine);
        } else {
            if (options.isBoundaryLineScissors()) {
                logger.debug("Scissor on horizontal line is only printed when using PageSize A4 or A5");
            }
            // one line
            final GeneralPath horizontalLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            final int translatedY = translateY(y);
            horizontalLine.moveTo(leftXwithMargin, translatedY);
            horizontalLine.lineTo(rightXwithMargin, translatedY);
            g.draw(horizontalLine);
        }
    }

    private void drawVerticalBoundaryLine(final Graphics2D g, final int halfLineWidth, BoundaryLines boundaryLines) {
        final int x = paymentPartLowerLeft.getX();
        final int lowerY = paymentPartLowerLeft.getY();
        final int upperY = lowerY + getPaymentPartHeight() - boundaryLineWidth - halfLineWidth;

        final int lowerYwithMargin = boundaryLines == BoundaryLines.ENABLED_WITH_MARGINS ? lowerY + printMarginPx : lowerY;

        g.setStroke(new BasicStroke(boundaryLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        if (options.isBoundaryLineScissors()) {
            final double targetWidth = millimetersToPointsRounded(SCISSOR_LENGTH, dpi);
            final double scale = targetWidth / Scissor.getOrigWidth();
            final double scissorHeight = Scissor.getOrigHeight() * scale;
            final int scissorUpperY = upperY - quietSpacePx;
            final int scissorLowerY = scissorUpperY - (int) targetWidth;

            AffineTransform at = new AffineTransform();
            final double scissorX = x + (scissorHeight / 2); // use half of the height of the image. as it is rotated, height becomes the width here
            at.translate(scissorX, translateY(scissorUpperY));
            at.rotate(Math.toRadians(90));
            at.scale(scale, scale);

            drawScissor(g, at);

            // two lines - leave out a space for the scissor
            final GeneralPath lowerVerticalLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            lowerVerticalLine.moveTo(x, translateY(lowerYwithMargin));
            lowerVerticalLine.lineTo(x, translateY(scissorLowerY));
            g.draw(lowerVerticalLine);

            final GeneralPath upperVerticalLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            upperVerticalLine.moveTo(x, translateY(upperY));
            upperVerticalLine.lineTo(x, translateY(scissorUpperY));
            g.draw(upperVerticalLine);
        } else {
            final GeneralPath verticalLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
            verticalLine.moveTo(x, translateY(lowerYwithMargin));
            verticalLine.lineTo(x, translateY(upperY));
            g.draw(verticalLine);
        }
    }

    private void drawScissor(final Graphics2D g, final AffineTransform at) {
        final AffineTransform previousTransform = g.getTransform();
        g.setTransform(at);
        Scissor.paint(g);

        // reset transform
        g.setTransform(previousTransform);
    }

    private void debugLayout(final Graphics2D g, final Rect<Integer> rect) {
        debugLayout(g, rect.getBottomY(), rect.getLeftX(), rect.getRightX(), rect.getTopY());
    }

    private void debugLayout(final Graphics2D g, final Integer bottomY, final Integer leftX, final Integer rightX, final Integer topY) {
        if (System.getProperty(SystemProperties.DEBUG_LAYOUT) != null) {
            final GeneralPath debugLine = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
            debugLine.moveTo(leftX, translateY(topY));
            debugLine.lineTo(rightX, translateY(topY));
            debugLine.lineTo(rightX, translateY(bottomY));
            debugLine.lineTo(leftX, translateY(bottomY));
            debugLine.closePath();
            g.setStroke(new BasicStroke(boundaryLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g.draw(debugLine);
        }
    }
}
