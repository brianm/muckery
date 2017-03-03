package org.skife.muckery.circuits;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Service implements AutoCloseable {
    private final ExecutorService threads = Executors.newCachedThreadPool();

    public <T> CompletableFuture<T> execute(Supplier<T> s) {
        CompletableFuture<T> f = new CompletableFuture<>();
        threads.submit(() -> {
            try {
                f.complete(s.get());
            } catch (Exception e) {
                f.completeExceptionally(e);
            }
        });
        return f;
    }

    @Override
    public void close() {
        threads.shutdown();
    }
}
