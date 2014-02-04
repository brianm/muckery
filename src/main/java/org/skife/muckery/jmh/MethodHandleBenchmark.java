package org.skife.muckery.jmh;

import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

@State
public class MethodHandleBenchmark
{
    private static long counter = 0;
    private static final Foo foo = new Foo(System.getenv().get("HOME"));
    private MethodHandle mh_virt;
    private Method method;
    private MethodHandle mh_unref;

    @Setup
    public void init() throws NoSuchMethodException, IllegalAccessException
    {
        counter = 0;
        method = Foo.class.getMethod("stuff");
        mh_virt = MethodHandles.lookup().findVirtual(Foo.class, "stuff", MethodType.methodType(void.class)).bindTo(foo);
        mh_unref = MethodHandles.lookup().unreflect(Foo.class.getMethod("stuff")).bindTo(foo);
    }

    @TearDown
    public void cleanup() {
        if (counter > 10000) {
            counter = 0;
        }
        else {
            counter = 1;
        }
    }

    @GenerateMicroBenchmark
    public void virtMethodHandle() throws Throwable
    {
        mh_virt.invokeExact();
    }

    @GenerateMicroBenchmark
    public void unrefMethodHandle() throws Throwable
    {
        mh_unref.invokeExact();
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
        private final String home;

        public Foo(String home)
        {
            this.home = home;
        }

        public void stuff()
        {
            if (home.startsWith("/usr")) {
                counter += 1;
            }
            else if (home.startsWith("/Home")) {
                counter += 2;
            }
            else if (home.startsWith("C:")) {
                counter += 3;
            }
        }
    }
}
