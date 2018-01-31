package utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class FormatterTest {

    @Parameterized.Parameter(0)
    public BigDecimal amountToFormat;
    @Parameterized.Parameter(1)
    public String expectedFormattedAmount;

    @Parameterized.Parameters()
    public static Collection testData() {
        return Arrays.asList(new Object[][]{
                {new BigDecimal("1"), "£1.00"},
                {new BigDecimal("23.0"), "£23.00"},
                {new BigDecimal("3.00"), "£3.00"},
                {new BigDecimal("4.123"), "£4.12"}
        });
    }

    @Test
    public void formatMoneyShouldReturnFormattedAmount() {
        String result = Formatter.formatMoney(amountToFormat);
        assertThat(result, is(expectedFormattedAmount));
    }


}