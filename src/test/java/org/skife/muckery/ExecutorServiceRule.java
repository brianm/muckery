package org.skife.muckery;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ExecutorServiceRule extends AbstractExecutorService implements TestRule  {

    private final AtomicReference<ExecutorService> delegate = new AtomicReference<>();
    private final Supplier<ExecutorService> factory;

    public ExecutorServiceRule(final Supplier<ExecutorService> factory) {
        this.factory = factory;
    }

    public ExecutorServiceRule() {
        this(Executors::newCachedThreadPool);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final ExecutorService es = ExecutorServiceRule.this.factory.get();
                ExecutorServiceRule.this.delegate.set(es);
                try {
                    base.evaluate();
                }
                finally {
                    es.shutdownNow();
                }
            }
        };
    }

    @Override
    public void shutdown() {
        this.delegate.get().shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.get().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.get().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.get().isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.delegate.get().awaitTermination(timeout, unit);
    }

    @Override
    public void execute(final Runnable command) {
        this.delegate.get().execute(command);
    }
}
