package org.skife.muckery.mvel;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mvel2.MVEL;

import static org.assertj.core.api.Assertions.assertThat;

public class MvelTest
{
    @Test
    public void testFoo() throws Exception
    {
        boolean not_brian = MVEL.eval("name != 'Brian'",
                                      ImmutableMap.of("name", "Ian"),
                                      Boolean.class);

        assertThat(not_brian).isTrue();
    }
}
