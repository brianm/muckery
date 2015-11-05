package org.skife.muckery.hdr;

import org.HdrHistogram.Histogram;
import org.junit.Test;

public class HdrHistogramTest
{
    @Test
    public void testFoo() throws Exception
    {
        Histogram h1 = new Histogram(3600000000000L, 3);
        Histogram h2 = new Histogram(3600000000000L, 3);

        h1.add(h2);

    }
}
