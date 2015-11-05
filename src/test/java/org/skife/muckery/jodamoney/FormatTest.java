package org.skife.muckery.jodamoney;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatTest
{
    @Test
    public void testFoo() throws Exception
    {
        long with_minor_units = 12300000;
        long major_units_only = 123000;

        Money money = Money.of(CurrencyUnit.EUR, BigDecimal.valueOf(with_minor_units, 2));
        MoneyFormatter f = new MoneyFormatterBuilder().appendCurrencySymbolLocalized()
                                                      .appendLiteral(" ")
                                                      .appendAmountLocalized()
                                                      .toFormatter();
        String joda_formatted = f.withLocale(new Locale("ca", "ES")).print(money);
        assertThat(joda_formatted).isEqualTo("€ 123.000,00");

        String java_formatted = NumberFormat.getCurrencyInstance(new Locale("ca", "ES"))
                                            .format(major_units_only);

        assertThat(java_formatted).isEqualTo(joda_formatted);
    }
}
