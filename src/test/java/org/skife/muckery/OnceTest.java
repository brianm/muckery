package org.skife.muckery;

import org.junit.Test;

import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class OnceTest {
    @Test
    public void testFoo() throws Exception {
        final AtomicInteger call_count = new AtomicInteger(0);
        final FutureTask<Integer> task = new FutureTask<>(call_count::incrementAndGet);

        task.run();
        task.run();

        assertThat(call_count.get()).isEqualTo(1);
    }
}
