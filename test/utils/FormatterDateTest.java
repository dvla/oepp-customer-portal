package utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FormatterDateTest {

    @Test
    public void formatDateShouldReturnCorrectlyFormattedDate() {
        Date date = DateTime.now().withYear(2015).withMonthOfYear(12).withDayOfMonth(30).toDate();
        String result = Formatter.formatDate(date, Locale.ENGLISH);
        assertThat(result, is("30 December 2015"));
    }

    @Test
    public void formatDateShouldReturnCorrectlyFormattedWelshDate() {
        Date date = DateTime.now().withYear(2015).withMonthOfYear(12).withDayOfMonth(30).toDate();
        String result = Formatter.formatDate(date, Locale.forLanguageTag("cy"));
        assertThat(result, is("30 Rhagfyr 2015"));
    }

    @Test
    public void formatDateShouldThrowNullPointerWithMeaningfulMessageWhenDateIsNull() {
        Date date = null;
        try {
            Formatter.formatDate(date, Locale.ENGLISH);
            fail("NullPointerException is expected");
        } catch (NullPointerException npe) {
            assertThat(npe.getMessage(), is("Date cannot be null"));
        }
    }

}