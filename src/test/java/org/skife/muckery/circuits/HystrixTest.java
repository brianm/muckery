package org.skife.muckery.circuits;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HystrixTest {

    private Service service;

    @Before
    public void setUp() throws Exception {
        service = new Service();
    }

    @After
    public void tearDown() throws Exception
    {
        service.close();
    }

    @Test
    public void testFoo() throws Exception {

        CompletableFuture<String> f = service.execute(() -> "hello world");
        assertThat(f).isCompleted()
                     .isCompletedWithValue("hello world");
    }

    @Test
    public void testWithCommand() throws Exception {
        ServiceCommand s1 = new ServiceCommand(service, () -> {throw new IllegalStateException("1");});

        ServiceCommand s2 = new ServiceCommand(service, () -> "2");

        ServiceCommand s3 = new ServiceCommand(service, () -> "3");

        assertThatThrownBy(s1::execute).hasCauseInstanceOf(TimeoutException.class);
        s2.execute();
        s3.execute();
    }

    @Ignore
    @Test
    public void testWithSempaphore() throws Exception {
        SemCommand s1 = new SemCommand(service, () -> {throw new IllegalStateException("1");});

        SemCommand s2 = new SemCommand(service, () -> "2");

        SemCommand s3 = new SemCommand(service, () -> "3");

        assertThatThrownBy(s1::execute).hasCauseInstanceOf(TimeoutException.class);
        s2.execute();
        s3.execute();
    }

    public static class ServiceCommand extends HystrixCommand<String> {

        private final Service service;
        private final Supplier<String> arg;

        public ServiceCommand(Service service, Supplier<String> arg) {
            super(HystrixCommandGroupKey.Factory.asKey("service"));
            this.service = service;
            this.arg = arg;
        }

        @Override
        protected String run() throws Exception {
            return service.execute(arg).get();
        }
    }

    public static class SemCommand extends HystrixCommand<String> {

        private final Service service;
        private final Supplier<String> arg;

        public SemCommand(Service service, Supplier<String> arg) {
            super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("semaphore"))
                                       .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                                                                                             .withExecutionTimeoutEnabled(true)
                                                                                             .withCircuitBreakerEnabled(true)
                                                                                             .withExecutionTimeoutInMilliseconds(1)
                                                                                             .withCircuitBreakerErrorThresholdPercentage(1)
                                                                                             .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
            this.service = service;
            this.arg = arg;
        }

        @Override
        protected String run() throws Exception {
            return service.execute(arg).get();
        }
    }
}
