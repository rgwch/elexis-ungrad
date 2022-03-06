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
package ch.codeblock.qrinvoice.model;

import ch.codeblock.qrinvoice.model.annotation.QrchPath;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.util.CollectionUtils;
import ch.codeblock.qrinvoice.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public final class SwissPaymentsCode {
    /**
     * Swiss Payments Code - Header - QRType
     */
    public static final String QR_TYPE = "SPC";

    /**
     * Swiss Payments Code - Header - Version 2.00
     */
    public static final String SPC_VERSION = "0200";
    private static final Short SPC_VERSION_MAJOR = extractMajorVersion(SPC_VERSION).orElseThrow(RuntimeException::new);

    /**
     * Swiss Payments Code - Header - Coding<br>
     * Coding 1 = indicates UTF-8 restricted to the Latin character set - according to the spec 4.3.3 - Data elements in the QR-bill - v2.0
     */
    public static final byte CODING_TYPE = 1;

    /**
     * Unambiguous indicator for the end of payment data. Fixed value "EPD" (End Payment Data). - according to the spec 4.3.3 - Data elements in the QR-bill - v2.0
     */
    public static final String END_PAYMENT_DATA_TRAILER = "EPD";
    public static final Currency CHF = Currency.getInstance("CHF");
    public static final Currency EUR = Currency.getInstance("EUR");
    /**
     * Only CHF and EUR are supported according to the spec 4.3.2 - v2.0
     */
    public static final Set<Currency> SUPPORTED_CURRENCIES = Collections.unmodifiableSet(new HashSet<>(asList(CHF, EUR)));

    public static final String COUNTRY_CODE_SWITZERLAND = "CH";
    public static final String COUNTRY_CODE_LIECHTENSTEIN = "LI";
    public static final Set<String> COUNTRY_CODES_CH_LI = Collections.unmodifiableSet(new HashSet<>(asList(COUNTRY_CODE_SWITZERLAND, COUNTRY_CODE_LIECHTENSTEIN)));
    /**
     * Only CH and LI are supported as country code in the creditors IBAN number 4.3.2 - v2.0
     */
    public static final Set<String> SUPPORTED_IBAN_COUNTRIES = COUNTRY_CODES_CH_LI;

    /**
     * Alternative scheme parameters - Can be currently delivered a maximum of two times according to the spec 4.3.1 - Depiction conventions - v2.0
     */
    public static final int MAX_ALT_PMT = 2;
    /**
     * The maximum Swiss QR Code data content permitted is 997 characters (including the element separators) according to the spec 5.2 - Maximum data range and QR code version - v2.0
     */
    public static final int SWISS_PAYMENTS_CODE_MAX_LENGTH = 997;
    /**
     * Only the latin character set is supported, UTF-8 should be used for encoding - according to the spec 4.2.1 - Character set - v2.0
     */
    public static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * According to the spec 4.2.3 - Separator element - v2.0
     */
    public static final String ELEMENT_SEPARATOR = "\n";

    /**
     * According to the spec 4.2.3 - Separator element - v2.0:<br>
     * "Note: Instead of the characters CR + LF, the LF character can be used alone (see also the FAQ at www.paymentstandards.ch/FAQ)"<br>
     * We relax it to all ASCII based newline separation possibilities
     */
    public static final String ELEMENT_SEPARATOR_REGEX_PATTERN = "\\r\\n|\\n|\\r";

    public static final String VALID_CHARACTERS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]_`abcdefghijklmnopqrstuvwxyz{}~£´ÀÁÂÄÇÈÉÊËÌÍÎÏÑÒÓÔÖÙÚÛÜßàáâäçèéêëìíîïñòóôö÷ùúûüý";

    /**
     * According to the spec 4.3.3 - Data elements in the QR-bill - v2.0:<br>
     * The amount element is to be entered without leading zeroes, including decimal separators and two decimal places. Decimal, maximum 12-digits permitted, including decimal separators.
     * Only decimal points (".") are permitted as decimal separators.
     */
    public static final String AMOUNT_DECIMAL_FORMAT = "#######.##";
    /**
     * Decimal separator according to the spec 4.3.3 - Amount section - v2.0
     */
    public static final char AMOUNT_DECIMAL_FORMAT_DECIMAL_SEPARATOR = '.';
    public static final BigDecimal AMOUNT_MIN = BigDecimal.ZERO;
    public static final BigDecimal AMOUNT_MAX = BigDecimal.valueOf(999999999.99);

    public static final String BILL_INFORMATION_REGEX_PATTERN = "^//[^/]{2,2}(/.*)?$";

    // Header
    @QrchPath("Header/QRType")
    private String qrType;
    @QrchPath("Header/Version")
    private String version;
    @QrchPath("Header/Coding")
    private String coding;

    // CdtrInf
    @QrchPath("CdtrInf/IBAN")
    private String iban;

    // CdtrInf/Cdtr
    @QrchPath("CdtrInf/Cdtr/AdrTp")
    private String crAdrTp;
    @QrchPath("CdtrInf/Cdtr/Name")
    private String crName;
    @QrchPath("CdtrInf/Cdtr/StrtNmOrAdrLine1")
    private String crStrtNmOrAdrLine1;
    @QrchPath("CdtrInf/Cdtr/BldgNbOrAdrLine2")
    private String crBldgNbOrAdrLine2;
    @QrchPath("CdtrInf/Cdtr/PstCd")
    private String crPstCd;
    @QrchPath("CdtrInf/Cdtr/TwnNm")
    private String crTwnNm;
    @QrchPath("CdtrInf/Cdtr/Ctry")
    private String crCtry;

    // UltmtCdtr
    @QrchPath("UltmtCdtr/AdrTp")
    private String ucrAdrTp;
    @QrchPath("UltmtCdtr/Name")
    private String ucrName;
    @QrchPath("UltmtCdtr/StrtNmOrAdrLine1")
    private String ucrStrtNmOrAdrLine1;
    @QrchPath("UltmtCdtr/BldgNbOrAdrLine2")
    private String ucrBldgNbOrAdrLine2;
    @QrchPath("UltmtCdtr/PstCd")
    private String ucrPstCd;
    @QrchPath("UltmtCdtr/TwnNm")
    private String ucrTwnNm;
    @QrchPath("UltmtCdtr/Ctry")
    private String ucrCtry;

    // CcyAmt
    @QrchPath("CcyAmt/Amt")
    private String amt;
    @QrchPath("CcyAmt/Ccy")
    private String ccy;

    // UltmtDbtr
    @QrchPath("UltmtDbtr/AdrTp")
    private String udAdrTp;
    @QrchPath("UltmtDbtr/Name")
    private String udName;
    @QrchPath("UltmtDbtr/StrtNmOrAdrLine1")
    private String udStrtNmOrAdrLine1;
    @QrchPath("UltmtDbtr/BldgNbOrAdrLine2")
    private String udBldgNbOrAdrLine2;
    @QrchPath("UltmtDbtr/PstCd")
    private String udPstCd;
    @QrchPath("UltmtDbtr/TwnNm")
    private String udTwnNm;
    @QrchPath("UltmtDbtr/Ctry")
    private String udCtry;

    // RmtInf
    @QrchPath("RmtInf/Tp")
    private String tp;
    @QrchPath("RmtInf/Ref")
    private String ref;

    // RmtInf/AddInf
    @QrchPath("RmtInf/AddInf/Ustrd")
    private String ustrd;
    @QrchPath("RmtInf/AddInf/Trailer")
    private String trailer;
    @QrchPath("RmtInf/AddInf/StrdBkgInf")
    private String strdBkgInf;

    // AltPmtInf
    @QrchPath("AltPmtInf/AltPmt")
    private List<String> altPmts;

    public String getQrType() {
        return qrType;
    }

    public void setQrType(final String qrType) {
        this.qrType = qrType;
    }

    public String getCoding() {
        return coding;
    }

    public void setCoding(final String coding) {
        this.coding = coding;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(final String iban) {
        this.iban = iban;
    }

    public String getCrAdrTp() {
        return crAdrTp;
    }

    public void setCrAdrTp(final String crAdrTp) {
        this.crAdrTp = crAdrTp;
    }

    public String getCrName() {
        return crName;
    }

    public void setCrName(final String crName) {
        this.crName = crName;
    }

    public String getCrStrtNmOrAdrLine1() {
        return crStrtNmOrAdrLine1;
    }

    public void setCrStrtNmOrAdrLine1(final String crStrtNmOrAdrLine1) {
        this.crStrtNmOrAdrLine1 = crStrtNmOrAdrLine1;
    }

    public String getCrBldgNbOrAdrLine2() {
        return crBldgNbOrAdrLine2;
    }

    public void setCrBldgNbOrAdrLine2(final String crBldgNbOrAdrLine2) {
        this.crBldgNbOrAdrLine2 = crBldgNbOrAdrLine2;
    }

    public String getCrPstCd() {
        return crPstCd;
    }

    public void setCrPstCd(final String crPstCd) {
        this.crPstCd = crPstCd;
    }

    public String getCrTwnNm() {
        return crTwnNm;
    }

    public void setCrTwnNm(final String crTwnNm) {
        this.crTwnNm = crTwnNm;
    }

    public String getCrCtry() {
        return crCtry;
    }

    public void setCrCtry(final String crCtry) {
        this.crCtry = crCtry;
    }

    public String getUcrAdrTp() {
        return ucrAdrTp;
    }

    public void setUcrAdrTp(final String ucrAdrTp) {
        this.ucrAdrTp = ucrAdrTp;
    }

    public String getUcrName() {
        return ucrName;
    }

    public void setUcrName(final String ucrName) {
        this.ucrName = ucrName;
    }

    public String getUcrStrtNmOrAdrLine1() {
        return ucrStrtNmOrAdrLine1;
    }

    public void setUcrStrtNmOrAdrLine1(final String ucrStrtNmOrAdrLine1) {
        this.ucrStrtNmOrAdrLine1 = ucrStrtNmOrAdrLine1;
    }

    public String getUcrBldgNbOrAdrLine2() {
        return ucrBldgNbOrAdrLine2;
    }

    public void setUcrBldgNbOrAdrLine2(final String ucrBldgNbOrAdrLine2) {
        this.ucrBldgNbOrAdrLine2 = ucrBldgNbOrAdrLine2;
    }

    public String getUcrPstCd() {
        return ucrPstCd;
    }

    public void setUcrPstCd(final String ucrPstCd) {
        this.ucrPstCd = ucrPstCd;
    }

    public String getUcrTwnNm() {
        return ucrTwnNm;
    }

    public void setUcrTwnNm(final String ucrTwnNm) {
        this.ucrTwnNm = ucrTwnNm;
    }

    public String getUcrCtry() {
        return ucrCtry;
    }

    public void setUcrCtry(final String ucrCtry) {
        this.ucrCtry = ucrCtry;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(final String amt) {
        this.amt = amt;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(final String ccy) {
        this.ccy = ccy;
    }

    public String getUdAdrTp() {
        return udAdrTp;
    }

    public void setUdAdrTp(final String udAdrTp) {
        this.udAdrTp = udAdrTp;
    }

    public String getUdName() {
        return udName;
    }

    public void setUdName(final String udName) {
        this.udName = udName;
    }

    public String getUdStrtNmOrAdrLine1() {
        return udStrtNmOrAdrLine1;
    }

    public void setUdStrtNmOrAdrLine1(final String udStrtNmOrAdrLine1) {
        this.udStrtNmOrAdrLine1 = udStrtNmOrAdrLine1;
    }

    public String getUdBldgNbOrAdrLine2() {
        return udBldgNbOrAdrLine2;
    }

    public void setUdBldgNbOrAdrLine2(final String udBldgNbOrAdrLine2) {
        this.udBldgNbOrAdrLine2 = udBldgNbOrAdrLine2;
    }

    public String getUdPstCd() {
        return udPstCd;
    }

    public void setUdPstCd(final String udPstCd) {
        this.udPstCd = udPstCd;
    }

    public String getUdTwnNm() {
        return udTwnNm;
    }

    public void setUdTwnNm(final String udTwnNm) {
        this.udTwnNm = udTwnNm;
    }

    public String getUdCtry() {
        return udCtry;
    }

    public void setUdCtry(final String udCtry) {
        this.udCtry = udCtry;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(final String tp) {
        this.tp = tp;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(final String ref) {
        this.ref = ref;
    }

    public String getUstrd() {
        return ustrd;
    }

    public void setUstrd(final String ustrd) {
        this.ustrd = ustrd;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(final String trailer) {
        this.trailer = trailer;
    }

    public String getStrdBkgInf() {
        return strdBkgInf;
    }

    public void setStrdBkgInf(final String strdBkgInf) {
        this.strdBkgInf = strdBkgInf;
    }

    public List<String> getAltPmts() {
        return altPmts;
    }

    public void setAltPmts(final List<String> altPmts) {
        this.altPmts = altPmts;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public boolean isVersionSupported() {
        return majorVersionEquals(version, SPC_VERSION);
    }

    public static boolean isVersionSupported(String version) {
        return majorVersionEquals(version, SPC_VERSION);
    }

    public static boolean isVersionSupported(Short version) {
        if (version == null) {
            return false;
        }
        return SPC_VERSION_MAJOR.equals(extractMajorVersion(String.valueOf(version)).orElse(null));
    }

    public static Optional<Short> extractMajorVersion(String version) {
        if (version != null && version.length() == 4) {
            return Optional.of(Short.parseShort(version.substring(0, 2)));
        } else if (version != null && version.length() == 3) {
            return Optional.of(Short.parseShort(version.substring(0, 1)));
        } else {
            return Optional.empty();
        }
    }

    public static boolean majorVersionEquals(String versionA, String versionB) {
        if (StringUtils.length(versionA) == 4 && StringUtils.length(versionB) == 4) {
            final Optional<Short> majorVersionA = extractMajorVersion(versionA);
            final Optional<Short> majorVersionB = extractMajorVersion(versionB);
            if (majorVersionA.isPresent() && majorVersionB.isPresent()) {
                return Objects.equals(majorVersionA.get(), majorVersionB.get());
            }
        }
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SwissPaymentsCode that = (SwissPaymentsCode) o;
        return Objects.equals(qrType, that.qrType) &&
                Objects.equals(version, that.version) &&
                Objects.equals(coding, that.coding) &&
                Objects.equals(iban, that.iban) &&
                Objects.equals(crAdrTp, that.crAdrTp) &&
                Objects.equals(crName, that.crName) &&
                Objects.equals(crStrtNmOrAdrLine1, that.crStrtNmOrAdrLine1) &&
                Objects.equals(crBldgNbOrAdrLine2, that.crBldgNbOrAdrLine2) &&
                Objects.equals(crPstCd, that.crPstCd) &&
                Objects.equals(crTwnNm, that.crTwnNm) &&
                Objects.equals(crCtry, that.crCtry) &&
                Objects.equals(ucrAdrTp, that.ucrAdrTp) &&
                Objects.equals(ucrName, that.ucrName) &&
                Objects.equals(ucrStrtNmOrAdrLine1, that.ucrStrtNmOrAdrLine1) &&
                Objects.equals(ucrBldgNbOrAdrLine2, that.ucrBldgNbOrAdrLine2) &&
                Objects.equals(ucrPstCd, that.ucrPstCd) &&
                Objects.equals(ucrTwnNm, that.ucrTwnNm) &&
                Objects.equals(ucrCtry, that.ucrCtry) &&
                Objects.equals(amt, that.amt) &&
                Objects.equals(ccy, that.ccy) &&
                Objects.equals(udAdrTp, that.udAdrTp) &&
                Objects.equals(udName, that.udName) &&
                Objects.equals(udStrtNmOrAdrLine1, that.udStrtNmOrAdrLine1) &&
                Objects.equals(udBldgNbOrAdrLine2, that.udBldgNbOrAdrLine2) &&
                Objects.equals(udPstCd, that.udPstCd) &&
                Objects.equals(udTwnNm, that.udTwnNm) &&
                Objects.equals(udCtry, that.udCtry) &&
                Objects.equals(tp, that.tp) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(ustrd, that.ustrd) &&
                Objects.equals(trailer, that.trailer) &&
                Objects.equals(strdBkgInf, that.strdBkgInf) &&
                Objects.equals(altPmts, that.altPmts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qrType, version, coding,  //
                iban, //
                crAdrTp, crName, crStrtNmOrAdrLine1, crBldgNbOrAdrLine2, crPstCd, crTwnNm, crCtry,  //
                ucrAdrTp, ucrName, ucrStrtNmOrAdrLine1, ucrBldgNbOrAdrLine2, ucrPstCd, ucrTwnNm, ucrCtry,  //
                amt, ccy,  //
                udAdrTp, udName, udStrtNmOrAdrLine1, udBldgNbOrAdrLine2, udPstCd, udTwnNm, udCtry,  //
                tp, ref,  //
                ustrd, trailer, strdBkgInf,  //
                altPmts); //
    }

    public String toSwissPaymentsCodeString() {
        // only safety guards to make sure no unexpected whitespaces are printed to swiss payments code
        assertNoWhitespaces(qrType, "Header/QRType");
        assertNoWhitespaces(version, "Header/Version");
        assertNoWhitespaces(coding, "Header/Coding");
        assertNoWhitespaces(iban, "CdtrInf/IBAN");
        assertNoWhitespaces(crAdrTp, "CdtrInf/Cdtr/AdrTp");
        assertNoWhitespaces(ucrAdrTp, "UltmtCdtr/AdrTp");
        assertNoWhitespaces(udAdrTp, "UltmtDbtr/AdrTp");
        assertNoWhitespaces(amt, "CcyAmt/Amt");
        assertNoWhitespaces(ccy, "CcyAmt/Ccy");
        assertNoWhitespaces(ref, "RmtInf/Ref");
        assertNoWhitespaces(tp, "RmtInf/Tp");
        assertNoWhitespaces(trailer, "RmtInf/AddInf/Trailer");


        final StringBuilder sb = new StringBuilder();

        final List<String> altPmtsNoBlanks = (altPmts == null) ? Collections.emptyList() : altPmts.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        // According to the spec 4.2.4 - Delivery of the data elements - v2.0:
        // All data elements must be present. If there is no content for a data element, then at least one carriage return (CR + LF or LF) must take place.
        appendField(sb, qrType);
        appendField(sb, version);
        appendField(sb, coding);
        appendField(sb, iban);
        appendField(sb, crAdrTp);
        appendField(sb, crName);
        appendField(sb, crStrtNmOrAdrLine1);
        appendField(sb, crBldgNbOrAdrLine2);
        appendField(sb, crPstCd);
        appendField(sb, crTwnNm);
        appendField(sb, crCtry);
        appendField(sb, ucrAdrTp);
        appendField(sb, ucrName);
        appendField(sb, ucrStrtNmOrAdrLine1);
        appendField(sb, ucrBldgNbOrAdrLine2);
        appendField(sb, ucrPstCd);
        appendField(sb, ucrTwnNm);
        appendField(sb, ucrCtry);
        appendField(sb, amt);
        appendField(sb, ccy);
        appendField(sb, udAdrTp);
        appendField(sb, udName);
        appendField(sb, udStrtNmOrAdrLine1);
        appendField(sb, udBldgNbOrAdrLine2);
        appendField(sb, udPstCd);
        appendField(sb, udTwnNm);
        appendField(sb, udCtry);
        appendField(sb, tp);
        appendField(sb, ref);
        appendField(sb, ustrd);
        appendField(sb, trailer);
        appendField(sb, strdBkgInf, CollectionUtils.isNotEmpty(altPmtsNoBlanks));

        // According to the spec 4.2.4 - Delivery of the data elements - v2.0:
        // The sole exceptions to this are additional data elements marked with "A" (alternative scheme). These may be omitted if they are not being used.
        appendAltPmts(sb, altPmtsNoBlanks);

        return sb.toString();
    }

    public void assertNoWhitespaces(final String str, final String field) {
        if (StringUtils.containsWhitespace(str)) {
            throw new ValidationException("SwissPaymentsCode: " + field + " did unexpectedly contain whitespace(s)");
        }
    }

    private StringBuilder appendField(final StringBuilder sb, final String value) {
        return this.appendField(sb, value, true);
    }

    private StringBuilder appendField(final StringBuilder sb, final String value, final boolean appendElementSeparator) {
        if (value != null) {
            sb.append(value);
        }
        if (appendElementSeparator) {
            sb.append(ELEMENT_SEPARATOR);
        }
        return sb;
    }

    private void appendAltPmts(final StringBuilder sb, final List<String> altPmts) {
        if (CollectionUtils.isNotEmpty(altPmts)) {
            final Iterator<String> iterator = altPmts.stream().filter(Objects::nonNull).iterator();
            while (iterator.hasNext()) {
                final String altPmt = iterator.next();

                // According to the spec 4.2.4 - Delivery of the data elements - v2.0:
                //The last data element delivered may not be completed with a concluding carriage return (CR + LF or LF).
                appendField(sb, altPmt, iterator.hasNext());
            }
        }
    }

    @Override
    public String toString() {
        return "SwissPaymentsCode{" +
                "qrType='" + qrType + '\'' +
                ", version='" + version + '\'' +
                ", coding='" + coding + '\'' +
                ", iban='" + iban + '\'' +
                ", crAdrTp='" + crAdrTp + '\'' +
                ", crName='" + crName + '\'' +
                ", crStrtNmOrAdrLine1='" + crStrtNmOrAdrLine1 + '\'' +
                ", crBldgNbOrAdrLine2='" + crBldgNbOrAdrLine2 + '\'' +
                ", crPstCd='" + crPstCd + '\'' +
                ", crTwnNm='" + crTwnNm + '\'' +
                ", crCtry='" + crCtry + '\'' +
                ", ucrAdrTp='" + ucrAdrTp + '\'' +
                ", ucrName='" + ucrName + '\'' +
                ", ucrStrtNmOrAdrLine1='" + ucrStrtNmOrAdrLine1 + '\'' +
                ", ucrBldgNbOrAdrLine2='" + ucrBldgNbOrAdrLine2 + '\'' +
                ", ucrPstCd='" + ucrPstCd + '\'' +
                ", ucrTwnNm='" + ucrTwnNm + '\'' +
                ", ucrCtry='" + ucrCtry + '\'' +
                ", amt='" + amt + '\'' +
                ", ccy='" + ccy + '\'' +
                ", udAdrTp='" + udAdrTp + '\'' +
                ", udName='" + udName + '\'' +
                ", udStrtNmOrAdrLine1='" + udStrtNmOrAdrLine1 + '\'' +
                ", udBldgNbOrAdrLine2='" + udBldgNbOrAdrLine2 + '\'' +
                ", udPstCd='" + udPstCd + '\'' +
                ", udTwnNm='" + udTwnNm + '\'' +
                ", udCtry='" + udCtry + '\'' +
                ", tp='" + tp + '\'' +
                ", ref='" + ref + '\'' +
                ", ustrd='" + ustrd + '\'' +
                ", trailer='" + trailer + '\'' +
                ", strdBkgInf='" + strdBkgInf + '\'' +
                ", altPmts='" + altPmts + '\'' +
                '}';
    }

}
