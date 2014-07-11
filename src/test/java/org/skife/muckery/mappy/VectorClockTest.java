package org.skife.muckery.mappy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VectorClockTest
{
    @Test
    public void testFoo() throws Exception
    {
        VectorClock c1 = new VectorClock();
        c1.incrementVersion(1, 123L);

        VectorClock c2 = new VectorClock();
        c1.incrementVersion(1, 888L);



        assertThat(c1.compare(c2)).isEqualTo(Occurred.AFTER);
        assertThat(c2.compare(c1)).isEqualTo(Occurred.BEFORE);

    }
}
