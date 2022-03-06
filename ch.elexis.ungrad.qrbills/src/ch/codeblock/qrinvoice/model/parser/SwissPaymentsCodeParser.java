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
package ch.codeblock.qrinvoice.model.parser;

import ch.codeblock.qrinvoice.model.ParseException;
import ch.codeblock.qrinvoice.model.SwissPaymentsCode;
import ch.codeblock.qrinvoice.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static ch.codeblock.qrinvoice.model.SwissPaymentsCode.MAX_ALT_PMT;
import static ch.codeblock.qrinvoice.util.StringUtils.emptyStringAsNull;

public class SwissPaymentsCodeParser {
    private static final Pattern ELEMENT_SEPARATOR_PATTERN = Pattern.compile(SwissPaymentsCode.ELEMENT_SEPARATOR_REGEX_PATTERN);

    public static SwissPaymentsCodeParser create() {
        return new SwissPaymentsCodeParser();
    }

    /**
     * @param swissPaymentsCode The Swiss Payments Code String
     * @return the parse SwissPaymentsCode instance
     * @throws ParseException in case of any parsing exceptions or unexpected problems
     */
    public SwissPaymentsCode parse(String swissPaymentsCode) throws ParseException {
        try {
            return internalParse(swissPaymentsCode);
        } catch (ParseException parseException) {
            throw parseException;
        } catch (Exception e) {
            throw new ParseException("Unexpected exception occurred during Swiss Payment Code Parsing", e);
        }
    }

    private SwissPaymentsCode internalParse(final String swissPaymentsCode) {
        if (swissPaymentsCode == null) {
            throw new ParseException("Encountered unexpected null instead of SwissPaymentsCode String");
        }

        try (final Scanner scanner = new Scanner(swissPaymentsCode).useDelimiter(ELEMENT_SEPARATOR_PATTERN)) {
            final SwissPaymentsCode spc = new SwissPaymentsCode();

            // Header
            spc.setQrType(read(scanner));
            if (!SwissPaymentsCode.QR_TYPE.equals(spc.getQrType())) {
                throw new ParseException("QR-Type '" + SwissPaymentsCode.QR_TYPE + "' expected, but '" + spc.getQrType() + "' encountered");
            }
            spc.setVersion(read(scanner));
            if (!spc.isVersionSupported()) {
                throw new ParseException("Version '" + SwissPaymentsCode.SPC_VERSION + "' (or subversions <2 digits majorversion><2 digits subversion>) expected, but '" + spc.getVersion() + "' encountered");
            }
            spc.setCoding(read(scanner));
            if (!String.valueOf(SwissPaymentsCode.CODING_TYPE).equals(spc.getCoding())) {
                throw new ParseException("Coding Type '" + SwissPaymentsCode.CODING_TYPE + "' expected, but '" + spc.getCoding() + "' encountered");
            }

            // CdtrInf
            spc.setIban(read(scanner));

            // CdtrInf/Cdtr
            spc.setCrAdrTp(read(scanner));
            spc.setCrName(read(scanner));
            spc.setCrStrtNmOrAdrLine1(read(scanner));
            spc.setCrBldgNbOrAdrLine2(read(scanner));
            spc.setCrPstCd(read(scanner));
            spc.setCrTwnNm(read(scanner));
            spc.setCrCtry(read(scanner));

            // UltmtCdtr
            spc.setUcrAdrTp(read(scanner));
            spc.setUcrName(read(scanner));
            spc.setUcrStrtNmOrAdrLine1(read(scanner));
            spc.setUcrBldgNbOrAdrLine2(read(scanner));
            spc.setUcrPstCd(read(scanner));
            spc.setUcrTwnNm(read(scanner));
            spc.setUcrCtry(read(scanner));

            // CcyAmt
            spc.setAmt(read(scanner));
            spc.setCcy(read(scanner));

            // UltmtDbtr
            spc.setUdAdrTp(read(scanner));
            spc.setUdName(read(scanner));
            spc.setUdStrtNmOrAdrLine1(read(scanner));
            spc.setUdBldgNbOrAdrLine2(read(scanner));
            spc.setUdPstCd(read(scanner));
            spc.setUdTwnNm(read(scanner));
            spc.setUdCtry(read(scanner));

            // RmtInf
            spc.setTp(read(scanner));
            spc.setRef(read(scanner));

            // RmtInf/AddInf
            spc.setUstrd(read(scanner));
            spc.setTrailer(read(scanner));
            spc.setStrdBkgInf(read(scanner));

            // AltPmtInf
            spc.setAltPmts(readAltPmts(scanner));

            // Check for end of payment code string
            // For parsing we are tolerant and accept up to 10 empty lines
            for (int additionalLineNr = 1; additionalLineNr <= 10; additionalLineNr++) {
                final String additionalLine = read(scanner);
                if (StringUtils.isNotBlank(additionalLine)) {
                    throw new ParseException("No lines after alternative payment methods expected, but additional non blank lines encountered");
                }
            }

            // after that additional, empty lines no more lines must exist
            if (scanner.hasNext()) {
                throw new ParseException("No lines after alternative payment methods expected, but additional lines encountered");
            }

            // Validate trailer string
            if (!SwissPaymentsCode.END_PAYMENT_DATA_TRAILER.equals(spc.getTrailer())) {
                throw new ParseException("Trailer '" + SwissPaymentsCode.END_PAYMENT_DATA_TRAILER + "' expected, but '" + spc.getTrailer() + "' encountered");
            }

            return spc;
        }
    }

    private List<String> readAltPmts(final Scanner scanner) {
        List<String> altPmts = null;
        String altPmt;
        for (int cnt = 0; (altPmt = read(scanner)) != null; ++cnt) {
            if (altPmts == null) {
                altPmts = new ArrayList<>();
            }

            altPmts.add(altPmt);

            // Maximum alt pmt
            if (cnt + 1 == MAX_ALT_PMT) {
                break;
            }
        }

        return altPmts;
    }

    private String read(final Scanner scanner) {
        if (scanner.hasNext()) {
            return emptyStringAsNull(scanner.next());
        } else {
            return null;
        }
    }
}
