package org.skife.muckery.money;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTest
{
    @Test
    public void testFoo() throws Exception
    {
Money money = Money.parse("USD 5.23");
Money more = money.plus(Money.of(CurrencyUnit.USD,
                                 new BigDecimal("1.23")));

Money java_zone = more.convertedTo(CurrencyUnit.getInstance("NOK"),
                                   new BigDecimal("6.07"),
                                   RoundingMode.HALF_UP);

assertThat(java_zone.toString()).isEqualTo("NOK 39.21");
    }
}
