package org.skife.muckery.jmx;

import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class MBeanTest {
    @Test
    public void testFoo() throws Exception {
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();
        Thread t = new Thread(() -> {
        });
        t.start();
        t.join();
        assertThat(ManagementFactory.getThreadMXBean().getThreadCount()).isEqualTo(threads);
    }
}
