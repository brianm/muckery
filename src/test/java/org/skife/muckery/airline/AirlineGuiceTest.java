package org.skife.muckery.airline;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import io.airlift.airline.Cli;
import io.airlift.airline.Command;
import org.junit.Test;

import java.util.List;

public class AirlineGuiceTest {
    @Test
    public void testFoo() throws Exception {
        final Injector guice = Guice.createInjector((Module) b -> {
            b.bindConstant().annotatedWith(Names.named("mock")).to("BLAH");
            b.bindConstant().annotatedWith(Names.named("greet")).to("Hello");
            b.bindConstant().annotatedWith(Names.named("name")).to("Brian");

            b.bind(Runnable.class).annotatedWith(Names.named("greeter")).to(Greeter.class);
            b.bind(Runnable.class).annotatedWith(Names.named("mocker")).to(Mocker.class);
        });

        List<Binding<Runnable>> sayers = guice.findBindingsByType(new TypeLiteral<Runnable>(){});
        System.out.println(sayers.get(0).getProvider().get().getClass());

        sayers.forEach(s -> s.getProvider().get().run());
    }


    @Test
    public void testBar() throws Exception {
        final Injector guice = Guice.createInjector((Module) b -> {
            b.bindConstant().annotatedWith(Names.named("mock")).to("bleh");
            b.bindConstant().annotatedWith(Names.named("greet")).to("Hello");
            b.bindConstant().annotatedWith(Names.named("name")).to("Brian");

            b.bind(Runnable.class).annotatedWith(Names.named("greeter")).to(Greeter.class);
            b.bind(Runnable.class).annotatedWith(Names.named("mocker")).to(Mocker.class);
        });

        List<Binding<Runnable>> runnables = guice.findBindingsByType(new TypeLiteral<Runnable>(){});
        for (Binding<Runnable> maybe : runnables) {
            maybe.acceptTargetVisitor(new DefaultBindingTargetVisitor<Runnable, Boolean>() {

            });
        }


        Cli<Runnable> cli = Cli.<Runnable>builder("foo").build();

//        cli.parse(type -> , "woof").run();;
    }


    @Command(name="greeter")
    public static class Greeter implements Runnable {

        private final String name;
        private final String greeting;

        @Inject
        Greeter(@Named("name") String name, @Named("greet") String greeting) {
            this.name = name;
            this.greeting = greeting;
        }

        public void run() {
            System.out.printf("%s, %s!\n", greeting, name);
        }
    }

    @Command(name="mocker")
    public static class Mocker implements Runnable {

        private final String name;
        private final String jibe;

        @Inject
        Mocker(@Named("name") String name, @Named("mock") String greeting) {
            this.name = name;
            this.jibe = greeting;
        }

        public void run() {
             System.out.printf("%s, %s!\n", jibe, name);
        }
    }
}
