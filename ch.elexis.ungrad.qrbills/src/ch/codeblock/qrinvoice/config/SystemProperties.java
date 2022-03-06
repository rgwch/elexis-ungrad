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
package ch.codeblock.qrinvoice.config;

public final class SystemProperties {
    public static final String DEBUG_LAYOUT = "QrInvoice.DebugLayout";
    public static final String IGNORE_LAYOUT_ERRORS = "QrInvoice.IgnoreLayoutErrors";
    public static final String STRICT_VALIDATION = "QrInvoice.StrictValidation";
    public static final String DISABLE_BILL_INFORMATION_VALIDATION = "QrInvoice.DisableBillInformationValidation";
    public static final String DISABLE_DO_NOT_USE_FOR_PAYMENT_VALIDATION = "QrInvoice.DisableDoNotUseForPaymentValidation";
    public static final String IGNORE_SYSTEM_FONTS = "QrInvoice.IgnoreSystemFonts";
    public static final String FONTS_DIRECTORY = "QrInvoice.FontDirectory";
    public static final String DISABLE_BANNER = "QrInvoice.DisableBanner";
    
    // In v2.0 of the specification in november 2018, the ultimate creditor is not allowed for use, but prepared for future use
    // this property unlocks the usage in this version
    public static final String UNLOCK_ULTIMATE_CREDITOR = "QrInvoice.UnlockUltimateCreditor";
    
    public static final String SCANNING_PDF_MAX_PAGES  = "QrInvoice.ScanningPdfMaxPages";
    
    // allows to disable ot CMM override
    public static final String DISABLE_PDFBOX_CMM_OVERRIDE  = "QrInvoice.DisablePdfBoxCmmOverride";
    
    // allows to remove the additional print margin - just in case
    public static final String DISABLE_ADDITIONAL_PRINT_MARGIN  = "QrInvoice.DisableAdditionalPrintMargin";
    
    private SystemProperties() {}
}
