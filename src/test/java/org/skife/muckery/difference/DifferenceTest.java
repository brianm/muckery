package org.skife.muckery.difference;

import com.google.common.collect.ImmutableMap;
import org.immutables.value.Value;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DifferenceTest {

    @Test
    public void testFoo() throws Exception {
        final Foo left = ImmutableFoo.builder()
                                     .name("George")
                                     .bar(ImmutableBar.builder()
                                                      .attributes(ImmutableMap.of("a", "1"))
                                                      .child(null)
                                                      .build())
                                     .build();

        final Foo right = ImmutableFoo.builder()
                                      .name("Jetson")
                                      .bar(ImmutableBar.builder()
                                                       .attributes(ImmutableMap.of("a", "1"))
                                                       .child(null)
                                                       .build())
                                      .build();

        final Set<Difference> diffs = ObjectDiff.differ().diff(left, right);
        assertThat(diffs).hasSize(1);
    }

    @Value.Immutable
    public interface Foo {
        String name();

        Bar bar();
    }

    @Value.Immutable
    public interface Bar {
        Map<String, String> attributes();

        Bar child();
    }
}
