package org.skife.muckery.dagger;

import dagger.ObjectGraph;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class DaggerTest
{
    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        ObjectGraph graph = ObjectGraph.create(new MyMod());
        Bar bar = graph.get(Bar.class);
        Bar bar2 = graph.get(Bar.class);
        assertThat(bar).isSameAs(bar2);

    }

    // @Module(complete = false)
    public static class MyMod
    {
        // @Provides @Singleton
        Bar bar(Foo foo)
        {
            return new Bar(foo);
        }
    }

    public static class Bar
    {
        public Bar(Foo foo)
        {

        }
    }

    public static class Foo
    {
        @Inject
        public Foo() {}
    }
}