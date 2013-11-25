package org.skife.muckery.jmh;

import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

@State
public class ContentionBenchmark
{
    private static final Foo foo = new Foo();
    private MethodHandle mh;
    private Method method;

    @Setup
    public void init() throws NoSuchMethodException, IllegalAccessException
    {
        method = Foo.class.getMethod("stuff");
        mh = MethodHandles.lookup().findVirtual(Foo.class, "stuff", MethodType.methodType(void.class)).bindTo(foo);
//        mh = MethodHandles.lookup().unreflect(method).bindTo(foo).asFixedArity();
    }

    @GenerateMicroBenchmark
//    @OutputTimeUnit(TimeUnit.SECONDS)
//    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
//    @Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
//    @Fork(1)
    public void methodHandle() throws Throwable
    {
        mh.invokeExact();
    }

//    @GenerateMicroBenchmark
    public void invokeDirect() throws Throwable
    {
        foo.stuff();
    }

//    @GenerateMicroBenchmark
    public void reflect() throws Throwable
    {
        method.invoke(foo);
    }

    public static class Foo
    {
        public void stuff()
        {
            if (2 + 2 == 4) {
                int x = 5 + 7;
            }
        }
    }
}
