package org.skife.muckery.rxjava;

import org.junit.Test;
import rx.Observable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RxJavaTest
{
    @Test
    public void testFoo() throws Exception
    {
        ExecutorService ex = Executors.newFixedThreadPool(2);
        String[] letters = {"a", "b", "c", "d", "e"};
        Integer[] numbers = {1, 2, 3, 4, 5};


        Observable<String> lo = Observable.from(letters);
        Observable<Integer> no = Observable.from(numbers);

        lo.zipWith(no, (s, i) -> {
            StringBuilder b = new StringBuilder();
            for (int j = 0; j < i * i; j++) { b.append(s);}
            return b.toString();
        }).onErrorReturn(Throwable::getMessage)
          .reduce("", (s, s2) -> s + s2 + "\n")
          .forEach(System.out::println);

        ex.shutdownNow();
    }
}
