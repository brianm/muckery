package org.skife.muckery.circuits;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FailsafeTest {
    private Service service;
    private ScheduledExecutorService clock = Executors.newScheduledThreadPool(2);

    @Before
    public void setUp() throws Exception {
        service = new Service();
    }

    @After
    public void tearDown() throws Exception {
        clock.shutdown();
        service.close();
    }

    @Test
    public void testFoo() throws Exception {
        CircuitBreaker cb = new CircuitBreaker().failOn(RuntimeException.class)
                                                .withDelay(1, TimeUnit.SECONDS)
                                                .withFailureThreshold(1, 1);

        CompletableFuture<String> f = Failsafe.with(cb)
                                              .with(clock)
                                              .future(() -> service.execute(() -> "hello"));
        f.get();
        assertThat(f).isCompletedWithValue("hello");

        f = Failsafe.with(cb).with(clock).future(() -> service.execute(() -> {
            throw new RuntimeException("NooOoOooO");
        }));

        assertThatThrownBy(f::get).hasCauseInstanceOf(RuntimeException.class);

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        f = Failsafe.with(cb)
                    .with(clock)
                    .withFallback(Failsafer.bounce())
                    .future(() -> service.execute(() -> {
                        throw new RuntimeException("NooOoOooO");
                    }));

        assertThatThrownBy(f::get).hasCauseInstanceOf(CircuitBreakerOpenException.class);
    }
}
