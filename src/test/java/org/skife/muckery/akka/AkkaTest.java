package org.skife.muckery.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AkkaTest {
    @Test
    public void testFoo() throws Exception {
        ActorSystem system = ActorSystem.create("global");
        ActorRef do_math = system.actorOf(DoMath.props(2), "domath-7");
        Future<Object> f = Patterns.ask(do_math, 3, 1000);
        Object result = Await.result(f, Duration.create(5, TimeUnit.SECONDS));

        assertThat(result).isEqualTo(6);
        system.shutdown();
    }

    public static class DoMath extends AbstractActor {
        private final LoggingAdapter log = Logging.getLogger(context().system(), this);
        private final int magicNumber;

        public static Props props(int magicNumber) {
            return new RoundRobinPool(5).props(Props.create(DoMath.class, () -> new DoMath(magicNumber)));
        }

        public DoMath(int magicNumber) {
            this.magicNumber = magicNumber;
            receive(ReceiveBuilder.match(Integer.class, this::onInt)
                            .matchAny(this::onAny)
                            .build());
        }

        public void onAny(Object other) {
            log.info("received unknown message {} from {}", other, context().sender());
        }

        public void onInt(int value) {
            context().sender().tell(value * magicNumber, context().self());
        }
    }
}
