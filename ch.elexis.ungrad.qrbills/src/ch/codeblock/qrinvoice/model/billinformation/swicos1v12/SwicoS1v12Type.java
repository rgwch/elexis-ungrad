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
package ch.codeblock.qrinvoice.model.billinformation.swicos1v12;

import ch.codeblock.qrinvoice.model.ParseException;
import ch.codeblock.qrinvoice.model.billinformation.BillInformationType;
import ch.codeblock.qrinvoice.util.CollectionUtils;
import ch.codeblock.qrinvoice.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class SwicoS1v12Type implements BillInformationType {
    private static final Logger logger = LoggerFactory.getLogger(SwicoS1v12Type.class);
    /**
     * According to: https://www.bfs.admin.ch/bfs/en/home/registers/enterprise-register/enterprise-identification/uid-general/uid.html
     * <p>The UID is composed of 9 numbers, is randomly generated and contains no information about the enterprise (anonymous identifier). The Swiss origin of the number is indicated by the "CHE" prefix, which is the three-letter country code of the ISO 3166-1 standard. The last number refers to the check digit (C) determined by the Modulo 11 standard calculation method.</p>
     * <p>To make it easier to read, the prefix is separated from the numerical part by a dash. The numerical part of the UID is divided into three blocks of three numbers separated by a full stop:<br>
     * CHE-999.999.99P</p>
     *
     * <p>Swico however says:</p>
     * <p>Wert: 106017086 - UID CHE-106.017.086 ohne CHE-Präfix, ohne Trennzeichen und ohne MWST/TVA/IVA/VAT-Suffix</p>
     */
    public static final Pattern UID_PATTERN = Pattern.compile("\\d{9}");

    public static final String SEPARATOR_PREFIX = "//";
    public static final String PREFIX = "S1";
    public static final String COMBINED_PREFIX = SEPARATOR_PREFIX + PREFIX;

    public static final String FIELD_SEPARATOR = "/";

    public static final String TAG_VALUE_LIST_SEPARATOR = ";";
    public static final String TAG_VALUE_KEY_VALUE_SEPARATOR = ":";

    private static final SwicoS1v12Type INSTANCE = new SwicoS1v12Type();

    private SwicoS1v12Type() {
    }

    public static SwicoS1v12Type getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean supports(final String strdBkgInf) {
        return strdBkgInf != null && strdBkgInf.startsWith(COMBINED_PREFIX);
    }

    @Override
    public SwicoS1v12 parse(final String billInformation) {
        try {
            return internalParse(billInformation);
        } catch (RuntimeException e) {
            throw new ParseException("Error while parsing billInformation", e);
        }
    }


    public static DecimalFormat numberFormat() {
        final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        formatSymbols.setDecimalSeparator('.');

        final DecimalFormat decimalFormat = new DecimalFormat("######.##", formatSymbols);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMinimumIntegerDigits(1);
        return decimalFormat;
    }


    private static final Pattern NON_ESCAPED_SEPARATOR = Pattern.compile("(?<!\\\\)/");
    private static final String DATE_PATTERN = "yyMMdd";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static SwicoS1v12 internalParse(String strdBkgInf) {
        String tagValues;
        if (strdBkgInf.startsWith(COMBINED_PREFIX)) {
            tagValues = strdBkgInf.substring(COMBINED_PREFIX.length());
        } else {
            throw new ParseException(format("Expected bill information to start with '%s' but got '%s...'", COMBINED_PREFIX, Math.min(6, strdBkgInf.length())));
        }

        final Map<Tag, String> values = extractTags(tagValues);

        final SwicoS1v12 s = new SwicoS1v12();
        for (final Map.Entry<Tag, String> entry : values.entrySet()) {
            final Tag tag = entry.getKey();
            switch (tag) {
                case INVOICE_REFERENCE:
                    s.setInvoiceReference(entry.getValue());
                    break;
                case INVOICE_DATE:
                    s.setInvoiceDate(LocalDate.from(DF.parse(entry.getValue())));
                    break;
                case CUSTOMER_REFERENCE:
                    s.setCustomerReference(entry.getValue());
                    break;
                case VAT_UID_NUMBER:
                    s.setUidNumber(entry.getValue());
                    break;
                case VAT_DATE:
                    if (entry.getValue().length() == 6) {
                        s.setVatDateStart(LocalDate.from(DF.parse(entry.getValue())));
                    } else if (entry.getValue().length() == 12) {
                        s.setVatDateStart(LocalDate.from(DF.parse(entry.getValue().substring(0, 6))));
                        s.setVatDateEnd(LocalDate.from(DF.parse(entry.getValue().substring(6))));
                    } else {
                        throw new ParseException(format("Unable to parse date '%s', expected either one date (%s) or two dates (%s%s)", entry.getValue(), DATE_PATTERN, DATE_PATTERN, DATE_PATTERN));
                    }
                    break;
                case VAT_DETAILS:
                    s.setVatDetails(parseVatDetails(entry));
                    break;
                case IMPORT_TAX:
                    s.setImportTaxes(parseImportTaxes(entry));
                    break;
                case PAYMENT_CONDITIONS:
                    s.setPaymentConditions(parsePaymentConditions(entry));
                    break;
                case UNKNOWN:
                    throw new ParseException("Unknown tag encountered");
            }
            logger.debug("Tag={} TagValue={}", tag.getTagNr(), entry.getValue());
        }

        return s;
    }

    private static List<PaymentCondition> parsePaymentConditions(final Map.Entry<Tag, String> entry) {
        return valuePairStream(entry)
                .map(stringArray -> {
                    if (stringArray.length == 2) {
                        final BigDecimal cashDiscountPercentage = new BigDecimal(stringArray[0]);
                        final int eligiblePaymentPeriodDays = Integer.parseInt(stringArray[1]);
                        return new PaymentCondition(cashDiscountPercentage, eligiblePaymentPeriodDays);
                    } else {
                        throw new ParseException(format("Expected a two-valued payment condition (e.g. '3.0:10' or '0:30') but got %s", Arrays.toString(stringArray)));
                    }
                })
                .collect(Collectors.toList());
    }

    private static Stream<String[]> valuePairStream(final Map.Entry<Tag, String> entry) {
        return extractValueList(entry.getValue()).stream()
                .map(SwicoS1v12Type::extractValuePair)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static List<ImportTaxPosition> parseImportTaxes(final Map.Entry<Tag, String> entry) {
        return valuePairStream(entry)
                .map(stringArray -> {
                    if (stringArray.length == 2) {
                        final BigDecimal taxPercentage = new BigDecimal(stringArray[0]);
                        final BigDecimal taxedAmount = new BigDecimal(stringArray[1]);
                        return new ImportTaxPosition(taxPercentage, taxedAmount);
                    } else {
                        throw new ParseException(format("Expected a two-valued import tax position (e.g. '7.7:16.15') but got %s", Arrays.toString(stringArray)));
                    }
                })
                .collect(Collectors.toList());
    }

    private static List<VatDetails> parseVatDetails(final Map.Entry<Tag, String> entry) {
        return valuePairStream(entry)
                .map(stringArray -> {
                    if (stringArray.length == 2) {
                        final BigDecimal taxPercentage = new BigDecimal(stringArray[0]);
                        final BigDecimal taxedNetAmount = new BigDecimal(stringArray[1]);
                        return new VatDetails(taxPercentage, taxedNetAmount);
                    } else if (stringArray.length == 1) {
                        final BigDecimal taxPercentage = new BigDecimal(stringArray[0]);
                        return new VatDetails(taxPercentage);
                    } else {
                        throw new ParseException(format("Expected one single- or multiple two-valued vat details positions (e.g. '7.7' or '8:1000') but got %s", Arrays.toString(stringArray)));
                    }
                })
                .collect(Collectors.toList());
    }

    private static Map<Tag, String> extractTags(final String tagValues) {
        final List<Integer> positions = new ArrayList<>();
        final Matcher matcher = NON_ESCAPED_SEPARATOR.matcher(tagValues);
        while (matcher.find()) {
            positions.add(matcher.start());
        }

        final Map<Tag, String> values = new LinkedHashMap<>();
        Tag tag = null;
        // always even runs (e.g. 2 tags -> loops 4 times)
        // e.g. first run expects tag, second run expects tag value, third run expects tag again ... 
        for (int i = 0; i < positions.size(); i++) {
            final int beginIndex = positions.get(i) + 1;
            final String matchedValue;
            if (i < (positions.size() - 1)) {
                final int endIndex = positions.get(i + 1);
                matchedValue = tagValues.substring(beginIndex, endIndex);
            } else {
                matchedValue = tagValues.substring(beginIndex);
            }

            // tag is null when tag is expected, tag is set when value for tag is expected
            if (tag == null) {
                tag = Tag.of(Integer.parseInt(matchedValue));
                if (tag == Tag.UNKNOWN) {
                    throw new ParseException(format("Unknown Tag '%s'", matchedValue));
                }
            } else {
                if (values.containsKey(tag)) {
                    throw new ParseException(format("Tag %s (%s) previously encountered, must not be set more than once", tag.getTagNr(), tag.name()));
                } else if (StringUtils.isEmpty(matchedValue)) {
                    // ignore tag as value is empty
                    tag = null;
                } else {
                    values.put(tag, unescapeValue(matchedValue));
                    tag = null;
                }
            }
        }
        return values;
    }

    static String unescapeValue(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\\\/", "/").replaceAll("\\\\\\\\", "\\\\");
    }

    static String escapeValue(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\\\", "\\\\\\\\").replaceAll("/", "\\\\/");
    }

    static List<String> extractValueList(String valueList) {
        if (StringUtils.isEmpty(valueList)) {
            return Collections.emptyList();
        }

        return Arrays.asList(valueList.split(TAG_VALUE_LIST_SEPARATOR));
    }

    static Optional<String[]> extractValuePair(String value) {
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value.split(TAG_VALUE_KEY_VALUE_SEPARATOR));
    }

    static String toStrdBkgInfString(SwicoS1v12 s) {
        final Map<Tag, String> values = new LinkedHashMap<>();

        if (s.getInvoiceReference() != null) {
            values.put(Tag.INVOICE_REFERENCE, s.getInvoiceReference());
        }
        if (s.getInvoiceDate() != null) {
            values.put(Tag.INVOICE_DATE, s.getInvoiceDate().format(DF));
        }
        if (s.getCustomerReference() != null) {
            values.put(Tag.CUSTOMER_REFERENCE, s.getCustomerReference());
        }
        if (s.getUidNumber() != null) {
            values.put(Tag.VAT_UID_NUMBER, s.getUidNumber());
        }

        if (s.getVatDateStart() != null) {
            if (s.getVatDateEnd() != null) {
                values.put(Tag.VAT_DATE, s.getVatDateStart().format(DF) + s.getVatDateEnd().format(DF));
            } else {
                values.put(Tag.VAT_DATE, s.getVatDateStart().format(DF));
            }
        }

        final DecimalFormat df = numberFormat();
        if (CollectionUtils.isNotEmpty(s.getVatDetails())) {
            final String vatDetailsTagValue = s.getVatDetails().stream()
                    .map(vatDetail -> {
                        if (vatDetail.getTaxedNetAmount() != null) {
                            return df.format(vatDetail.getTaxPercentage()) + TAG_VALUE_KEY_VALUE_SEPARATOR + df.format(vatDetail.getTaxedNetAmount());
                        } else {
                            return df.format(vatDetail.getTaxPercentage());
                        }
                    })
                    .collect(Collectors.joining(TAG_VALUE_LIST_SEPARATOR));
            values.put(Tag.VAT_DETAILS, vatDetailsTagValue);
        }


        if (s.getImportTaxes() != null) {
            final String taxImportTagValue = s.getImportTaxes().stream()
                    .map(importTaxPosition -> df.format(importTaxPosition.getTaxPercentage()) + TAG_VALUE_KEY_VALUE_SEPARATOR + df.format(importTaxPosition.getTaxAmount()))
                    .collect(Collectors.joining(TAG_VALUE_LIST_SEPARATOR));
            values.put(Tag.IMPORT_TAX, taxImportTagValue);
        }

        if (s.getPaymentConditions() != null) {
            final String paymentConditionValue = s.getPaymentConditions().stream()
                    .map(paymentCondition -> df.format(paymentCondition.getCashDiscountPercentage()) + TAG_VALUE_KEY_VALUE_SEPARATOR + paymentCondition.getEligiblePaymentPeriodDays())
                    .collect(Collectors.joining(TAG_VALUE_LIST_SEPARATOR));
            values.put(Tag.PAYMENT_CONDITIONS, paymentConditionValue);
        }

        final StringBuilder sb = new StringBuilder(COMBINED_PREFIX);
        values.forEach((key, value) -> sb.append(FIELD_SEPARATOR)
                .append(key.getTagNr())
                .append(FIELD_SEPARATOR)
                .append(escapeValue(value)));
        return sb.toString();
    }


}
