package utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FormatterDateTimeTest {

    @Test
    public void formatDateTime_shouldReturnCorrectlyFormattedDateTime() {
        Date date = DateTime.now().withYear(2015).withMonthOfYear(12).withDayOfMonth(30).withHourOfDay(11).withMinuteOfHour(27).withSecondOfMinute(31).toDate();
        String result = Formatter.formatDateTime(date, Locale.ENGLISH);
        assertThat(result, is("11:27:31 on 30 December 2015"));
    }

    @Test
    public void formatDateTime_shouldReturnCorrectlyFormattedWelshDateTime() {
        Date date = DateTime.now().withYear(2015).withMonthOfYear(12).withDayOfMonth(30).withHourOfDay(11).withMinuteOfHour(27).withSecondOfMinute(31).toDate();
        String result = Formatter.formatDateTime(date, Locale.forLanguageTag("cy"));
        assertThat(result, is("11:27:31 on 30 Rhagfyr 2015"));
    }

    @Test
    public void formatDateTime_shouldThrowNullPointerWithMeaningfulMessageWhenDateIsNull() {
        try {
            Formatter.formatDateTime(null, Locale.ENGLISH);
            fail("NullPointerException is expected");
        } catch (NullPointerException npe) {
            assertThat(npe.getMessage(), is("Date cannot be null"));
        }
    }

}