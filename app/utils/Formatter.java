package utils;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Formatter {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("£0.00");
    private static final String DATE_FORMAT = "d MMMM yyyy";
    private static final String DATE_TIME_FORMAT = "HH:mm:ss 'on' " + DATE_FORMAT;
    private static final String DATE_TIME_FORMAT_AM_PM = "H:mma";

    private Formatter() {
        throw new AssertionError("This class should not be instantiated");
    }

    public static String formatMoney(BigDecimal amount) {
        checkNotNull(amount, "Amount cannot be null");
        return MONEY_FORMAT.format(amount);
    }

    public static String formatDate(Date date, Locale locale) {
        checkNotNull(date, "Date cannot be null");
        return new SimpleDateFormat(DATE_FORMAT, getDateFormatSymbols(locale)).format(date);
    }

    public static String formatDateTime(Date date, Locale locale) {
        checkNotNull(date, "Date cannot be null");
        return new SimpleDateFormat(DATE_TIME_FORMAT, getDateFormatSymbols(locale)).format(date);
    }

   public static String formatTimeAmPm(Date date, Locale locale) {
       checkNotNull(date, "Date cannot be null");
       return new SimpleDateFormat(DATE_TIME_FORMAT_AM_PM, getDateFormatSymbols(locale)).format(date).toLowerCase();

   }

    private static DateFormatSymbols getDateFormatSymbols(Locale locale) {
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        if (locale.getLanguage().equals("cy")) {
            symbols.setMonths(new String[] {
                    "Ionawr",
                    "Chwefror",
                    "Mawrth",
                    "Ebrill",
                    "Mai",
                    "Mehefin",
                    "Gorffennaf",
                    "Awst",
                    "Medi",
                    "Hydref",
                    "Tachwedd",
                    "Rhagfyr",
            });
        }
        return symbols;
    }
}
