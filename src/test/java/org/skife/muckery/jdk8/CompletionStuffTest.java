package org.skife.muckery.jdk8;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class CompletionStuffTest
{
    @Test
    public void testFoo() throws Exception
    {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new ForkJoinTask<String>()
        {
            @Override
            public String getRawResult()
            {

            }

            @Override
            protected void setRawResult(final String value)
            {

            }

            @Override
            protected boolean exec()
            {
                return false;
            }
        })
        ExecutorService es = Executors.newCachedThreadPool();
        try {
            ExecutorCompletionService ecs = new ExecutorCompletionService(es);

            CompletableFuture<String> cf = new CompletableFuture<>();
        }
        finally {

            es.shutdown();

        }
    }
}
