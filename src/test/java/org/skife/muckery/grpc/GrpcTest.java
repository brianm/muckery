package org.skife.muckery.grpc;

import com.google.api.client.util.Lists;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.muckery.NetUtil;
import org.skife.muckery.grpc.hello.Greeting;
import org.skife.muckery.grpc.hello.HelloServiceGrpc;
import org.skife.muckery.grpc.hello.Person;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcTest {

    private ManagedChannel channel;
    private Server server;

    @Before
    public void setUp() throws Exception {
        int port = NetUtil.findUnusedPort();

        server = InProcessServerBuilder.forPort(port)
                                       .addService(HelloServiceGrpc.bindService(new HelloService()))
                                       .build()
                                       .start();


        channel = InProcessChannelBuilder.forAddress("127.0.0.1", port)
                                         .usePlaintext(true)
                                         .build();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        channel.shutdown();
    }

    @Test
    public void testBlockingStub() throws Exception {

        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
        Greeting greeting = stub.greet(Person.newBuilder()
                                             .setName("Brian")
                                             .build());

        assertThat(greeting.getMessage()).isEqualTo("Hello, Brian");

    }

    @Test
    public void testHelloFutureStub() throws Exception {
        HelloServiceGrpc.HelloServiceFutureStub stub = HelloServiceGrpc.newFutureStub(channel);


        ListenableFuture<Greeting> greeting = stub.greet(Person.newBuilder()
                                                               .setName("Brian")
                                                               .build());

        assertThat(greeting.get().getMessage()).isEqualTo("Hello, Brian");

    }

    @Test
    public void testStreamingClient() throws Exception {
        HelloServiceGrpc.HelloServiceStub stub = HelloServiceGrpc.newStub(channel);

        Set<String> flags = Sets.newConcurrentHashSet();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> msg = new AtomicReference<>();
        StreamObserver<Person> obs = stub.greetEveryone(new StreamObserver<Greeting>() {
            @Override
            public void onNext(Greeting greeting) {
                msg.set(greeting.getMessage());
                flags.add("response");
            }

            @Override
            public void onError(Throwable throwable) {
                flags.add("error");
            }

            @Override
            public void onCompleted() {
                flags.add("complete");
                latch.countDown();
            }
        });
        obs.onNext(Person.newBuilder().setName("Joy").build());
        obs.onNext(Person.newBuilder().setName("Brian").build());
        obs.onCompleted();

        latch.await(1, TimeUnit.SECONDS);
        assertThat(msg.get()).isEqualTo("Hello, Joy, Brian!");
        assertThat(flags).containsExactly("response", "complete");
    }

    private static class HelloService implements HelloServiceGrpc.HelloService {

        @Override
        public void greet(final Person request, final StreamObserver<Greeting> responseObserver) {
            String name = request.getName();
            if ("Fred".equals(name)) {
                responseObserver.onError(new IllegalArgumentException("No Freds Allowed"));
            }
            else {
                responseObserver.onNext(Greeting.newBuilder().setMessage("Hello, " + name).build());
                responseObserver.onCompleted();
            }
        }

        @Override
        public StreamObserver<Person> greetEveryone(StreamObserver<Greeting> ro) {
            return new StreamObserver<Person>() {

                private List<String> names = Lists.newArrayList();

                @Override
                public void onNext(Person person) {
                    names.add(person.getName());
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {
                    ro.onNext(Greeting.newBuilder()
                                      .setMessage("Hello, " + Joiner.on(", ").join(names) + "!")
                                      .build());
                    ro.onCompleted();
                }
            };
        }
    }
}
