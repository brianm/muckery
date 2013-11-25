package org.skife.muckery.method_handles;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodHandleTest
{

    @Test
    public void testFoo() throws Throwable
    {
        Foo foo = new Foo();
        MethodHandle mh = MethodHandles.lookup().bind(foo, "bar", MethodType.methodType(int.class));
        int answer = (int) mh.invokeExact();
        assertThat(answer).isEqualTo(42);
    }

    public static class Foo
    {
        public int bar() {
            return 42;
        }
    }
}
