package org.skife.muckery.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class CircuitBreakerTest {
    @Test
    public void testFoo() {
        CircuitBreaker breaker = CircuitBreaker.ofDefaults("testFoo");
        assertThat(breaker.getCircuitBreakerConfig()
                          .getFailureRateThreshold()).isBetween(49f, 51f);
        Consumer<String> cc = CircuitBreaker.decorateConsumer(breaker, (s) -> {});
        cc.accept("hello world");
    }
}
