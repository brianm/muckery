package org.skife.muckery.lombok;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LombokTest {

    @Test
    public void testFoo() throws Exception {
        Lombo l = new Lombo();
        l.setName("woof");
        assertThat(l.getName()).isEqualTo("woof");
    }

    public static class Lombo {
        @Getter
        @Setter
        String name;
    }
}
