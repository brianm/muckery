package org.skife.muckery.rx;

import io.reactivex.Observable;
import org.junit.Test;

public class RxTest {
    @Test
    public void testFoo() throws Exception {

        Observable<String> obs = Observable.create(emitter -> {
            emitter.onNext("one");
            emitter.onNext("two");
            emitter.onNext("three");
            emitter.onError(new IllegalArgumentException("oops"));
            emitter.onComplete();
        });

        obs.subscribe(System.out::println,
                      err -> System.out.println(err.getMessage()),
                      () -> System.out.println("done"));


    }
}
