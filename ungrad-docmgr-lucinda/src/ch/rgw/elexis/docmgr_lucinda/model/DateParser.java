/**
 * Originally based on BalusC's DateUtil (@link http://balusc.blogspot.com/2007/09/dateutil.html)
 * extended with some patterns for usage in Germany, Austria and Switzerland
 */
package ch.rgw.elexis.docmgr_lucinda.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.rgw.tools.TimeTool;

public class DateParser {
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\d{8}$", "yyyyMMdd"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "dd.MM.yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\s[a-zA-Z]{3}\\s\\d{4}$", "dd MMM yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}[\\s\\.]+[a-zA-Z]{3}\\s\\d{4}$", "dd. MMM yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\s[a-zA-Z]{4,}\\s\\d{4}$", "dd MMMM yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\.\\s[a-zA-Z]{4,}\\s\\d{4}$", "dd. MMMM yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{12}$", "yyyyMMddHHmm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}[\\s,]+\\d{1,2}:\\d{2}$", "dd.MM.yyyy HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}-\\d{2}-\\d{2}t\\d{1,2}:\\d{2}:\\d{2}", "yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}-\\d{2}-\\d{2}t\\d{1,2}:\\d{2}:\\d{2}z", "yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\.\\s[a-z]{3}\\s\\d{4},\\s\\d{1,2}:\\d{2}$", "dd. MMM yyyy, HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\.\\s[a-z]{4,}\\s\\d{4},\\s\\d{1,2}:\\d{2}$", "dd. MMMM yyyy, HH:mm"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{14}$", "yyyyMMddHHmmss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss"); //$NON-NLS-1$ //$NON-NLS-2$
    }};

    /**
     * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
     * format is unknown. You can simply extend DateUtil with more formats if needed.
     *
     * @param dateString The date string to determine the SimpleDateFormat pattern for.
     * @return The matching SimpleDateFormat pattern, or null if format is unknown.
     * @see SimpleDateFormat
     */
    public static String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }
        return null; // Unknown format.
    }


    /**
     * Parse the given date string to date object and return a date instance based on the given
     * date string. This makes use of the {@link DateParser#determineDateFormat(String)} to determine
     * the SimpleDateFormat pattern to be used for parsing.
     *
     * @param dateString The date string to be parsed to date object.
     * @return The parsed date object.
     * @throws ParseException If the date format pattern of the given date string is unknown, or if
     *                        the given date string or its actual date is invalid based on the date format pattern.
     */

    public static TimeTool parse(String dateString) throws ParseException {
        String dateFormat = determineDateFormat(dateString);
        if (dateFormat == null) {
            throw new ParseException(Messages.DateParser_unknown_date_format, 0);
        }
        return new TimeTool(parse(dateString, dateFormat));
    }

    /**
     * Validate the actual date of the given date string based on the given date format pattern and
     * return a date instance based on the given date string.
     *
     * @param dateString The date string.
     * @param dateFormat The date format pattern which should respect the SimpleDateFormat rules.
     * @return The parsed date object.
     * @throws ParseException If the given date string or its actual date is invalid based on the
     *                        given date format pattern.
     * @see SimpleDateFormat
     */
    public static Date parse(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        simpleDateFormat.setLenient(false); // Don't automatically convert invalid date.
        return simpleDateFormat.parse(dateString);
    }

}
