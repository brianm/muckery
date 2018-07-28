package org.skife.muckery;

import org.junit.Test;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamTest {
    @Test
    public void testFoo() throws Exception {

        Stream.Builder<String> builder = Stream.<String>builder();


    }

    public static class Producer<T> implements Spliterator<T> {

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }
    }
}
