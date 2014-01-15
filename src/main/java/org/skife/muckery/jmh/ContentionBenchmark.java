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
    private MethodHandle mh_virt;
    private Method method;
    private MethodHandle mh_unref;

    @Setup
    public void init() throws NoSuchMethodException, IllegalAccessException
    {
        method = Foo.class.getMethod("stuff");
        mh_virt = MethodHandles.lookup().findVirtual(Foo.class, "stuff", MethodType.methodType(void.class)).bindTo(foo);
        mh_unref = MethodHandles.lookup().unreflect(Foo.class.getMethod("stuff")).bindTo(foo);
    }

    @GenerateMicroBenchmark
    public void virtMethodHandle() throws Throwable
    {
        mh_virt.invokeExact();
    }

    @GenerateMicroBenchmark
    public void unrefMethodHandle() throws Throwable
    {
        mh_virt.invokeExact();
    }

    @GenerateMicroBenchmark
    public void invokeDirect() throws Throwable
    {
        foo.stuff();
    }

    @GenerateMicroBenchmark
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
